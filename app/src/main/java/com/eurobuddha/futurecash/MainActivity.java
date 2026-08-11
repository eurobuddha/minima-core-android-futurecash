package com.eurobuddha.futurecash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.minimarex.minimaapi.MinimaAPI;
import org.minimarex.minimaapi.MinimaAPIMessages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * FutureCash — native time-locked payments. Talks to the local Minima node over the broadcast-Intent IPC
 * (same transport as vestr). On pairing it registers the FutureCash script ({@code newscript}) — the
 * byte-identical contract, so the returned address (and coins) interoperate with the web dapp. Three tabs:
 * Send / Future / About.
 */
public class MainActivity extends AppCompatActivity {

    static final String NODE_PKG = "org.minimarex.minimacore";
    public static final int TAB_SEND = 0, TAB_FUTURE = 1, TAB_ABOUT = 2;

    private NodeApi node;
    private ViewPager viewPager;
    private MainPager pager;
    private BaseView[] views;
    private LinearLayout pairingBanner, bottomBar;
    private TextView blockNo;
    private final LinearLayout[] tabs = new LinearLayout[3];
    private BroadcastReceiver notifyReceiver;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable reloadTask = this::reload;

    // ---- node-derived state ----
    private String scriptAddress = "";
    private int chainBlock = 0;
    private final List<JSONObject> contractCoins = new ArrayList<>();   // raw coins at the script address
    private boolean resolving = false;   // an address resolution is in flight (don't stack them)
    private boolean registerTried = false;   // newscript has been attempted once this session
    private boolean scanned = false;     // a coin scan has completed at least once
    private int malformed = 0;           // coins found at the contract that payments() had to reject

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Edge-to-edge (targetSdk 35): push the header below the status bar and the nav below the nav
        // bar, with the dark header/nav backgrounds filling the system-bar areas.
        final View root = findViewById(R.id.main);
        final View headerV = findViewById(R.id.header);
        final View navV = findViewById(R.id.bottomBar);
        final int headerTop = headerV.getPaddingTop();
        final int navBottom = navV.getPaddingBottom();
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets b = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            headerV.setPadding(headerV.getPaddingLeft(), headerTop + b.top, headerV.getPaddingRight(), headerV.getPaddingBottom());
            navV.setPadding(navV.getPaddingLeft() + b.left, navV.getPaddingTop(), navV.getPaddingRight() + b.right, navBottom + b.bottom);
            return insets;
        });
        androidx.core.view.ViewCompat.requestApplyInsets(root);
        new androidx.core.view.WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightStatusBars(false);

        pairingBanner = findViewById(R.id.pairingBanner);
        bottomBar = findViewById(R.id.bottomBar);
        blockNo = findViewById(R.id.blockNo);
        ((Button) findViewById(R.id.openNodeBtn)).setOnClickListener(v -> openMinimaCore());

        views = new BaseView[]{ new SendView(this), new FutureView(this), new AboutView(this) };
        pager = new MainPager(views, new String[]{"Send", "Future", "About"});
        viewPager = findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pager);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int pos) { setActiveTab(pos); views[pos].onShown(); }
        });
        buildBottomBar();
        setActiveTab(TAB_SEND);

        node = new NodeApi(this, enabled -> {
            if (enabled) { setPaired(true); ensureScriptAddress(); }
            else setPaired(false);
        });

        notifyReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent intent) {
                if (!MinimaAPI.checkMinimaID(MainActivity.this, intent)) return;
                String data = intent.getStringExtra(MinimaAPIMessages.MINIMA_API_NOTIFY_DATA);
                if (data == null) return;
                try {
                    String event = new JSONObject(data).optString("event", "");
                    if ("NEWBLOCK".equals(event) || "NEWBALANCE".equals(event)) requestReload();
                } catch (Exception ignored) {}
            }
        };
        ContextCompat.registerReceiver(this, notifyReceiver,
                new IntentFilter(MinimaAPIMessages.MINIMA_API_NOTIFY), ContextCompat.RECEIVER_EXPORTED);
    }

    @Override protected void onResume() { super.onResume(); requestReload(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ui.removeCallbacks(reloadTask);
        if (node != null) node.onDestroy();
        if (notifyReceiver != null) try { unregisterReceiver(notifyReceiver); } catch (Exception ignored) {}
    }

    // ===== bottom nav =====

    private void buildBottomBar() {
        String[] glyphs = {"➤", "⏳", "ⓘ"};
        String[] labels = {"Send", "Future", "About"};
        for (int i = 0; i < 3; i++) {
            final int pos = i;
            LinearLayout tab = new LinearLayout(this);
            tab.setOrientation(LinearLayout.VERTICAL);
            tab.setGravity(Gravity.CENTER);
            tab.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tab.setPadding(0, dp(6), 0, dp(6));
            TextView glyph = new TextView(this);
            glyph.setText(glyphs[i]); glyph.setTextSize(20f); glyph.setGravity(Gravity.CENTER);
            TextView label = new TextView(this);
            label.setText(labels[i]); label.setTextSize(11f); label.setGravity(Gravity.CENTER);
            label.setLetterSpacing(0.05f); label.setPadding(0, dp(2), 0, 0);
            tab.addView(glyph); tab.addView(label);
            tab.setOnClickListener(v -> viewPager.setCurrentItem(pos));
            bottomBar.addView(tab);
            tabs[i] = tab;
        }
    }

    private void setActiveTab(int active) {
        for (int i = 0; i < tabs.length; i++) {
            int color = i == active ? FcDesign.NAV_ACTIVE : FcDesign.NAV_INACTIVE;
            ((TextView) tabs[i].getChildAt(0)).setTextColor(color);
            ((TextView) tabs[i].getChildAt(1)).setTextColor(color);
        }
    }

    public void goToTab(int pos) { viewPager.setCurrentItem(pos); }

    // ===== contract script deploy + reload =====

    /**
     * Resolve the FutureCash script address, retried on every reload until it sticks.
     *
     * This used to run ONCE, off the pairing callback. If that single {@code enabled:true} reply never
     * arrived (already-registered app, node restarted mid-session, a timeout) the address stayed blank,
     * {@link #reload()} returned early forever, and the app showed an empty list with no error — a real
     * stake looked like no stake. Resolution is now idempotent and re-driven from reload().
     *
     * Reads {@code scripts} FIRST and registers only when the script is genuinely missing. {@code newscript}
     * is NOT an idempotent no-op — core does {@code removeScript()} then {@code addScript()} as two separate
     * calls, so re-registering on every launch opens a window with no FutureCash script at all (a
     * {@code txnbasics} landing in it attaches no script and the spend is rejected on-chain).
     */
    private void ensureScriptAddress() {
        if (resolving || !scriptAddress.isEmpty()) return;
        resolving = true;
        node.cmd("scripts", new NodeApi.Cb() {
            @Override public void onResult(JSONObject json) {
                resolving = false;
                JSONArray arr = json.optJSONArray("response");
                String found = "";
                if (arr != null) for (int i = 0; i < arr.length(); i++) {
                    JSONObject s = arr.optJSONObject(i);
                    if (s == null) continue;
                    String addr = s.optString("address", "");
                    // Match on ADDRESS first, text second. `clean:true` — what the MiniDapp registers with —
                    // makes core store the script as Contract.cleanScript re-emits it, with its own spacing
                    // and hex-case rules. A whitespace-collapse text compare misses that perfectly good row
                    // and sends us off to register a duplicate.
                    if (FutureCashContract.KNOWN_ADDRESS.equalsIgnoreCase(addr)
                            || FutureCashContract.SCRIPT.equals(
                                    s.optString("script", "").replaceAll("\\s+", " ").trim())) {
                        found = addr;
                        break;
                    }
                }
                if (!found.isEmpty()) { scriptAddress = found; afterAddress(); }
                else if (!registerTried) { registerTried = true; registerScript(); }
                else for (BaseView v : views) v.refresh();   // give up quietly; emptyReason explains
            }
            @Override public void onError(String message) { resolving = false; handleErr(message); }
        });
    }

    /** Add the script — only reached when {@code scripts} proved it absent, and only ONCE per session.
     *  Without that cap a node that never hands back an address would have reload() → ensureScriptAddress()
     *  → newscript loop on every block, rewriting the script table forever. */
    private void registerScript() {
        resolving = true;
        node.cmd("newscript script:\"" + FutureCashContract.SCRIPT + "\" trackall:false", new NodeApi.Cb() {
            @Override public void onResult(JSONObject json) {
                resolving = false;
                JSONObject r = json.optJSONObject("response");
                if (r != null) scriptAddress = r.optString("address", scriptAddress);
                if (!scriptAddress.isEmpty()) afterAddress();
                else for (BaseView v : views) v.refresh();
            }
            @Override public void onError(String message) { resolving = false; handleErr(message); }
        });
    }

    private void afterAddress() {
        reload();
        for (BaseView v : views) v.refresh();
    }

    /** Every address worth scanning: what this node derives, plus the canonical one as a backstop. */
    private List<String> scanAddresses() {
        List<String> out = new ArrayList<>();
        if (!scriptAddress.isEmpty()) out.add(scriptAddress);
        if (!FutureCashContract.KNOWN_ADDRESS.equalsIgnoreCase(scriptAddress)) {
            out.add(FutureCashContract.KNOWN_ADDRESS);
        }
        return out;
    }

    public void requestReload() {
        ui.removeCallbacks(reloadTask);
        ui.postDelayed(reloadTask, 400);
    }

    public void reload() {
        node.cmd("block", new NodeApi.Cb() {
            @Override public void onResult(JSONObject json) {
                setPaired(true);
                JSONObject r = json.optJSONObject("response");
                if (r != null) {
                    String b = r.optString("block", "");
                    try { chainBlock = Integer.parseInt(b); } catch (Exception ignored) {}
                    blockNo.setText("#" + chainBlock);
                }
            }
            @Override public void onError(String message) { handleErr(message); }
        });

        // No address yet: keep trying rather than silently showing an empty list.
        if (scriptAddress.isEmpty()) { ensureScriptAddress(); return; }
        scanCoins();
    }

    /**
     * Scan every candidate address for our relevant coins and merge the results.
     *
     * {@code relevant:true} is deliberate and load-bearing. We register with {@code trackall:false}, so the
     * contract address is NOT in {@code Wallet.mAllTrackedAddress} and {@code TxPoWTreeNode.checkRelevant}
     * keeps a stake only because state port 2 — its payout — is an address this wallet tracks. Core's
     * relevance flag IS the "mine, not a stranger's" filter, applied before the coin ever reaches us.
     *
     * The per-address filter also keeps each reply small: a bare {@code coins relevant:true} returns the whole
     * wallet, and this transport caps inbound at 256K chars.
     */
    private void scanCoins() {
        final List<String> addrs = scanAddresses();
        final List<JSONObject> merged = new ArrayList<>();
        final HashSet<String> seen = new HashSet<>();
        final int[] pending = { addrs.size() };
        for (String a : addrs) {
            node.cmd("coins address:" + a + " relevant:true", new NodeApi.Cb() {
                @Override public void onResult(JSONObject json) {
                    setPaired(true);
                    JSONArray arr = json.optJSONArray("response");
                    if (arr != null) for (int i = 0; i < arr.length(); i++) {
                        JSONObject c = arr.optJSONObject(i);
                        if (c == null || c.optBoolean("spent", false)) continue;
                        String id = c.optString("coinid", "");
                        if (!id.isEmpty() && !seen.add(id)) continue;   // same coin via both addresses
                        merged.add(c);
                    }
                    if (--pending[0] == 0) publish(merged);
                }
                @Override public void onError(String message) {
                    if (--pending[0] == 0) publish(merged);
                    handleErr(message);
                }
            });
        }
    }

    /** Adopt a completed scan. Callbacks are marshalled to the main thread by NodeApi, so this is single-threaded. */
    private void publish(List<JSONObject> coins) {
        contractCoins.clear();
        contractCoins.addAll(coins);
        // Count what payments() will reject, so a coin with malformed on-chain state is reported rather
        // than silently disappearing between the node and the list.
        malformed = 0;
        for (JSONObject c : contractCoins) if (!FuturePayment.from(c).valid()) malformed++;
        scanned = true;
        for (BaseView v : views) v.refresh();
    }

    private void handleErr(String message) {
        if (NodeApi.ERR_NOT_ENABLED.equals(message)) setPaired(false);
        else Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ===== pairing UX =====

    private void setPaired(boolean paired) {
        pairingBanner.setVisibility(paired ? View.GONE : View.VISIBLE);
    }

    private void openMinimaCore() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(NODE_PKG);
        if (launch != null) startActivity(launch);
        else Toast.makeText(this, "Minima Core is not installed.", Toast.LENGTH_LONG).show();
    }

    // ===== accessors for views =====

    public NodeApi node() { return node; }
    public int chainBlock() { return chainBlock; }
    public String scriptAddress() { return scriptAddress; }
    public int currentTab() { return viewPager.getCurrentItem(); }

    /**
     * Why the list is empty — a blank "nothing here" is indistinguishable from a bug, which is exactly how a
     * real locked stake stayed invisible. Each branch names the step that produced nothing.
     */
    public String emptyReason() {
        if (scriptAddress.isEmpty()) {
            return "Finding the FutureCash contract on your node…\n\nIf this stays here, open Minima Core → Apps "
                    + "and check FutureCash is enabled.";
        }
        if (!scanned) return "Reading your locked coins…";
        if (malformed > 0) {
            return malformed + (malformed == 1 ? " coin was" : " coins were") + " found at the contract but "
                    + "carry malformed on-chain state, so they are not shown.\n\nContract " + Util.shorten(scriptAddress);
        }
        return "Nothing locked yet. Send Minima or a token to a future block.\n\nContract " + Util.shorten(scriptAddress)
                + "\nNo coins here are flagged relevant by this node.";
    }

    /** The contract address actually in use — surfaced in About so it can be compared with the MiniDapp's. */
    public String contractAddress() { return scriptAddress; }

    /**
     * Parsed future-cash payments (coins at the script address relevant to this wallet), NEWEST FIRST.
     *
     * The sort is ours because the node does not do it: core's coins.java only sorts when {@code order:asc}
     * is asked for — the default {@code "desc"} is a no-op branch — so the reply arrives in tree-walk order,
     * which scatters ready and locked payments through the list. Merging two address scans shuffles it
     * further still.
     *
     * Ordered by the block the coin was created in: on-chain, always present, and monotonic. The port-3
     * timestamp only breaks ties within a block, since it is sender-supplied state rather than chain truth.
     */
    public List<FuturePayment> payments() {
        List<FuturePayment> out = new ArrayList<>();
        for (JSONObject c : contractCoins) {
            FuturePayment p = FuturePayment.from(c);
            if (p.valid()) out.add(p);
        }
        Collections.sort(out, (a, b) -> {
            int byBlock = Long.compare(b.createdBlock, a.createdBlock);
            if (byBlock != 0) return byBlock;
            int byTime = Long.compare(b.createdMs, a.createdMs);
            if (byTime != 0) return byTime;
            // Same block and same stamp: coinid keeps the order stable across refreshes so cards
            // don't swap places under the user's finger.
            return String.valueOf(a.coinid).compareTo(String.valueOf(b.coinid));
        });
        return out;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
