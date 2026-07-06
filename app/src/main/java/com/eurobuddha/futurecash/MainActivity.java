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
            if (enabled) { setPaired(true); deployScript(); }
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

    /** Register the FutureCash script and adopt the returned address (the shared, interoperable address).
     *  Falls back to a {@code scripts} lookup if newscript doesn't hand back an address (e.g. the script
     *  was already registered) — so the address is never left blank. */
    private void deployScript() {
        node.cmd("newscript script:\"" + FutureCashContract.SCRIPT + "\" trackall:false", new NodeApi.Cb() {
            @Override public void onResult(JSONObject json) {
                JSONObject r = json.optJSONObject("response");
                if (r != null) {
                    String a = r.optString("address", "");
                    if (!a.isEmpty()) scriptAddress = a;
                }
                if (scriptAddress.isEmpty()) resolveScriptAddress();
                else { requestReload(); for (BaseView v : views) v.refresh(); }
            }
            @Override public void onError(String message) { resolveScriptAddress(); }
        });
    }

    /** Resolve the FutureCash address from the node's registered scripts — reliable whether the script was
     *  just registered or already present. */
    private void resolveScriptAddress() {
        node.cmd("scripts", new NodeApi.Cb() {
            @Override public void onResult(JSONObject json) {
                JSONArray arr = json.optJSONArray("response");
                if (arr != null) for (int i = 0; i < arr.length(); i++) {
                    JSONObject s = arr.optJSONObject(i);
                    if (s == null) continue;
                    if (FutureCashContract.SCRIPT.equals(s.optString("script", "").replaceAll("\\s+", " ").trim())) {
                        scriptAddress = s.optString("address", scriptAddress);
                        break;
                    }
                }
                requestReload();
                for (BaseView v : views) v.refresh();
            }
            @Override public void onError(String message) { handleErr(message); }
        });
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

        if (scriptAddress.isEmpty()) return;
        node.cmd("coins address:" + scriptAddress + " relevant:true", new NodeApi.Cb() {
            @Override public void onResult(JSONObject json) {
                setPaired(true);
                contractCoins.clear();
                JSONArray arr = json.optJSONArray("response");
                if (arr != null) for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.optJSONObject(i);
                    if (c != null) contractCoins.add(c);
                }
                for (BaseView v : views) v.refresh();
            }
            @Override public void onError(String message) { handleErr(message); }
        });
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

    /** Parsed future-cash payments (coins at the script address relevant to this wallet). */
    public List<FuturePayment> payments() {
        List<FuturePayment> out = new ArrayList<>();
        for (JSONObject c : contractCoins) {
            FuturePayment p = FuturePayment.from(c);
            if (p.valid()) out.add(p);
        }
        return out;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
