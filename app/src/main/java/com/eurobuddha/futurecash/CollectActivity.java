package com.eurobuddha.futurecash;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.json.JSONObject;
import org.minimarex.minimaapi.MinimaAPI;
import org.minimarex.minimaapi.MinimaAPIMessages;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Future-payment detail + the Collect transaction: spend the matured coin, paying the full amount to the
 *  recipient stored in state[2] (single-shot — no change, no state carried forward). */
public class CollectActivity extends SubActivity {

    private FuturePayment p;
    private int tip = 0;
    private boolean tipReady = false;
    private boolean collecting = false;          // true once a collect sequence is in flight
    private Boolean renderedReady = null;        // last maturity state we drew (avoids per-block rebuilds)
    private BroadcastReceiver blockReceiver;
    private EditText burnInput;
    private TextView collectBtn, status;

    @Override
    protected void init() {
        title("Future payment");
        try { p = FuturePayment.from(new JSONObject(getIntent().getStringExtra("coin"))); }
        catch (Exception e) { finish(); return; }
        render();
        fetchBlock();

        // Re-check the tip each block so a payment flips to collectable the moment it matures while open.
        blockReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                if (!MinimaAPI.checkMinimaID(CollectActivity.this, i)) return;
                String data = i.getStringExtra(MinimaAPIMessages.MINIMA_API_NOTIFY_DATA);
                if (data == null) return;
                try {
                    if ("NEWBLOCK".equals(new JSONObject(data).optString("event", "")) && !collecting) fetchBlock();
                } catch (Exception ignored) {}
            }
        };
        ContextCompat.registerReceiver(this, blockReceiver,
                new IntentFilter(MinimaAPIMessages.MINIMA_API_NOTIFY), ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        if (blockReceiver != null) try { unregisterReceiver(blockReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }

    private void fetchBlock() {
        node.cmd("block", new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                JSONObject r = j.optJSONObject("response");
                if (r != null) try { tip = Integer.parseInt(r.optString("block", "0")); } catch (Exception ignored) {}
                tipReady = true;
                maybeRender();
            }
            @Override public void onError(String m) { tipReady = true; maybeRender(); }
        });
    }

    /** Redraw only on first tip or when the ready/locked state actually flips — keeps any typed burn and
     *  never rebuilds the form out from under an in-flight collect. */
    private void maybeRender() {
        if (collecting) return;
        boolean ready = tipReady && p.matured(tip);
        if (renderedReady == null || renderedReady != ready) { renderedReady = ready; render(); }
    }

    private void render() {
        form.removeAllViews();
        boolean ready = tipReady && p.matured(tip);

        // hero
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable hb = new GradientDrawable();
        hb.setColor(ready ? FcDesign.GREEN_BG : FcDesign.CARD);
        hb.setCornerRadius(dp(10));
        hero.setBackground(hb);
        TextView hl = new TextView(this);
        hl.setText(ready ? "Ready to collect" : "Locked until the future block");
        hl.setTextColor(FcDesign.DIM);
        hl.setTextSize(13f);
        hero.addView(hl);
        TextView amountTv = new TextView(this);
        amountTv.setText(FcCardUi.amount(p.amount) + " " + p.tokenName);
        amountTv.setTextColor(ready ? FcDesign.GREEN : FcDesign.TEXT);
        amountTv.setTextSize(26f);
        amountTv.setTypeface(Typeface.DEFAULT_BOLD);
        hero.addView(amountTv);
        form.addView(hero);

        // stats
        addStat("Recipient", FcCardUi.shortHex(p.recipient));
        addStat("Unlock block", String.valueOf(p.futureBlock));
        if (tipReady) {
            long left = p.blocksRemaining(tip);
            addStat("Status", ready ? "ready now" : (left + " blocks  (≈ " + FcCardUi.blocksToSpan(left) + ")"));
            addStat("Chain tip", "#" + tip);
        }
        if (p.createdMs > 0) addStat("Created", FcCardUi.relative(p.createdMs, "created"));

        // copyable ids
        addCopy("Token id", p.tokenid);
        addCopy("Coin id", p.coinid);
        addCopy("Recipient address", p.recipient);

        label("Network fee (burn, optional)");
        burnInput = input("0", T_DEC);

        collectBtn = primaryButton(ready ? "Collect " + FcCardUi.amount(p.amount) + " " + p.tokenName
                : (!tipReady ? "Loading…" : "Locked — not ready yet"));
        collectBtn.setEnabled(ready);
        collectBtn.setAlpha(ready ? 1f : 0.4f);
        collectBtn.setOnClickListener(v -> collect());
        status = status();
    }

    private void addStat(String k, String v) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(8));
        TextView kt = new TextView(this);
        kt.setText(k);
        kt.setTextColor(FcDesign.DIM);
        kt.setTextSize(14f);
        kt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(kt);
        TextView vt = new TextView(this);
        vt.setText(v == null ? "" : v);
        vt.setTextColor(FcDesign.TEXT);
        vt.setTextSize(14f);
        vt.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(vt);
        form.addView(row);
    }

    private void addCopy(String k, final String v) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        TextView kt = new TextView(this);
        kt.setText(k);
        kt.setTextColor(FcDesign.DIM);
        kt.setTextSize(12f);
        row.addView(kt);
        TextView vt = new TextView(this);
        vt.setText(v);
        vt.setTextColor(FcDesign.TEXT);
        vt.setTextSize(12f);
        vt.setTypeface(Typeface.MONOSPACE);
        vt.setOnClickListener(view -> {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText(k, v));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        });
        row.addView(vt);
        form.addView(row);
    }

    // ---- collect ----

    private void collect() {
        if (!p.matured(tip)) return;
        String burn = burnInput.getText().toString().trim();
        if (!burn.isEmpty() && !isPositive(burn)) { setStatus(status, "Burn must be a positive number.", false); return; }
        String id = "fc" + System.currentTimeMillis();

        // Single-shot spend: the whole coin goes to the recipient (state[2]); no change, no state kept.
        List<String> cmds = new ArrayList<>();
        cmds.add("txncreate id:" + id);
        cmds.add("txninput id:" + id + " coinid:" + p.coinid + " scriptmmr:true");
        cmds.add("txnoutput id:" + id + " address:" + p.recipient + " amount:" + p.amount.toPlainString()
                + " tokenid:" + p.tokenid + " storestate:false");
        String post = "txnpost id:" + id;
        if (!burn.isEmpty() && isPositive(burn)) post += " burn:" + burn;
        cmds.add(post);

        collecting = true;
        collectBtn.setEnabled(false);
        setStatus(status, "Collecting…", true);
        runSequence(cmds, 0, id);
    }

    private void runSequence(final List<String> cmds, final int i, final String id) {
        if (i >= cmds.size()) {
            node.cmd("txndelete id:" + id, null);
            setStatus(status, "✓ Collected — funds will arrive shortly.", true);
            collectBtn.postDelayed(this::finish, 1400);
            return;
        }
        node.cmd(cmds.get(i), new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                // build steps return status:true; txnpost is async-mined (istransaction may be false — still ok).
                if (!j.optBoolean("status", true)) { fail(j.optString("error", "Collect failed"), id); return; }
                runSequence(cmds, i + 1, id);
            }
            @Override public void onError(String m) {
                fail(NodeApi.ERR_NOT_ENABLED.equals(m) ? "Enable FutureCash in Minima Core → Apps first." : m, id);
            }
        });
    }

    private void fail(String msg, String id) {
        node.cmd("txndelete id:" + id, null);
        collecting = false;
        collectBtn.setEnabled(true);
        setStatus(status, "Failed: " + msg, false);
    }

    private boolean isPositive(String s) { try { return new BigDecimal(s).signum() > 0; } catch (Exception e) { return false; } }
}
