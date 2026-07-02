package org.minimarex.futurecash;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/** Shared rendering for a future-payment list card (Send + Future tabs). Mirrors vestr's ContractUi. */
public final class FcCardUi {

    private FcCardUi() {}

    /** One tappable card: header (icon + amount + recipient) + body (status + timing); opens Collect on tap. */
    public static View card(final MainActivity act, final FuturePayment p) {
        int tip = act.chainBlock();
        boolean ready = p.matured(tip);
        int pad = dp(act, 14);

        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(FcDesign.CARD);
        bg.setCornerRadius(dp(act, 8));
        card.setBackground(bg);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.bottomMargin = dp(act, 12);
        card.setLayoutParams(clp);

        // ---- header: token icon + amount + recipient ----
        LinearLayout head = new LinearLayout(act);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setBackgroundColor(FcDesign.CARD_2);
        head.setPadding(pad, pad, pad, pad);

        TextView icon = new TextView(act);
        icon.setText(p.isMinima() ? "M" : (p.tokenName.isEmpty() ? "?" : p.tokenName.substring(0, 1).toUpperCase()));
        icon.setTextColor(FcDesign.ON_YELLOW);
        icon.setTextSize(18f);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setGravity(Gravity.CENTER);
        GradientDrawable ib = new GradientDrawable();
        ib.setColor(FcDesign.YELLOW);
        ib.setCornerRadius(dp(act, 6));
        icon.setBackground(ib);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(act, 44), dp(act, 44));
        ilp.rightMargin = dp(act, 12);
        icon.setLayoutParams(ilp);
        head.addView(icon);

        LinearLayout titleCol = new LinearLayout(act);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView amt = new TextView(act);
        amt.setText(amount(p.amount) + " " + p.tokenName);
        amt.setTextColor(FcDesign.TEXT);
        amt.setTextSize(16f);
        amt.setTypeface(Typeface.DEFAULT_BOLD);
        titleCol.addView(amt);
        TextView to = new TextView(act);
        to.setText("to " + shortHex(p.recipient));
        to.setTextColor(FcDesign.DIM);
        to.setTextSize(12f);
        to.setSingleLine(true);
        titleCol.addView(to);
        head.addView(titleCol);

        // status pill on the right of the header
        TextView pill = new TextView(act);
        pill.setText(ready ? "READY" : "LOCKED");
        pill.setTextColor(ready ? FcDesign.ON_YELLOW : FcDesign.DIM);
        pill.setTextSize(10f);
        pill.setTypeface(Typeface.DEFAULT_BOLD);
        pill.setLetterSpacing(0.08f);
        pill.setPadding(dp(act, 9), dp(act, 4), dp(act, 9), dp(act, 4));
        GradientDrawable pb = new GradientDrawable();
        pb.setColor(ready ? FcDesign.GREEN : FcDesign.BLACK_3);
        pb.setCornerRadius(dp(act, 20));
        pill.setBackground(pb);
        head.addView(pill);
        card.addView(head);

        // ---- body ----
        LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setPadding(pad, pad, pad, pad);

        LinearLayout left = new LinearLayout(act);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView st = new TextView(act);
        long left2 = p.blocksRemaining(tip);
        st.setText(ready ? "Ready to collect" : ("Unlocks in " + left2 + (left2 == 1 ? " block" : " blocks")));
        st.setTextColor(ready ? FcDesign.GREEN : FcDesign.TEXT);
        st.setTextSize(13f);
        st.setTypeface(Typeface.DEFAULT_BOLD);
        left.addView(st);
        TextView sub = new TextView(act);
        sub.setText(ready ? ("at block #" + p.futureBlock) : ("≈ " + blocksToSpan(left2)));
        sub.setTextColor(FcDesign.DIM);
        sub.setTextSize(12f);
        sub.setPadding(0, dp(act, 2), 0, 0);
        left.addView(sub);
        body.addView(left);

        LinearLayout right = new LinearLayout(act);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        right.setGravity(Gravity.END);
        TextView unlock = new TextView(act);
        unlock.setText("Unlock #" + p.futureBlock);
        unlock.setTextColor(FcDesign.DIM_2);
        unlock.setTextSize(12f);
        unlock.setGravity(Gravity.END);
        right.addView(unlock);
        if (p.createdMs > 0) {
            TextView created = new TextView(act);
            created.setText(relative(p.createdMs, "created"));
            created.setTextColor(FcDesign.DIM_2);
            created.setTextSize(12f);
            created.setGravity(Gravity.END);
            created.setPadding(0, dp(act, 2), 0, 0);
            right.addView(created);
        }
        body.addView(right);
        card.addView(body);

        card.setOnClickListener(v -> {
            Intent i = new Intent(act, CollectActivity.class);
            i.putExtra("coin", p.raw.toString());
            act.startActivity(i);
        });
        return card;
    }

    // ---- helpers ----

    static String amount(BigDecimal b) {
        try {
            BigDecimal t = b;
            if (b.scale() > 6) t = b.setScale(6, RoundingMode.DOWN);
            return t.stripTrailingZeros().toPlainString();
        } catch (Exception e) { return b.toPlainString(); }
    }

    static String shortHex(String s) {
        if (s == null) return "";
        s = s.startsWith("0x") ? s.substring(2) : s;
        return s.length() <= 10 ? s : "0x" + s.substring(0, 6) + "…" + s.substring(s.length() - 4);
    }

    /** Estimate a human span from a block count (~50 s/block). */
    static String blocksToSpan(long blocks) {
        if (blocks <= 0) return "now";
        long secs = blocks * FutureCashContract.SECONDS_PER_BLOCK;
        long days = secs / 86400, hours = secs / 3600, mins = secs / 60;
        if (days > 0) return days + (days == 1 ? " day" : " days");
        if (hours > 0) return hours + (hours == 1 ? " hour" : " hours");
        return Math.max(1, mins) + (mins == 1 ? " min" : " mins");
    }

    /** "created 2 days ago" / "created just now". */
    static String relative(long whenMs, String verb) {
        if (whenMs <= 0) return "";
        long diff = System.currentTimeMillis() - whenMs;
        long a = Math.abs(diff);
        long days = TimeUnit.MILLISECONDS.toDays(a);
        long hours = TimeUnit.MILLISECONDS.toHours(a);
        long mins = TimeUnit.MILLISECONDS.toMinutes(a);
        String span = days > 0 ? days + (days == 1 ? " day" : " days")
                : hours > 0 ? hours + (hours == 1 ? " hour" : " hours")
                : mins > 0 ? mins + (mins == 1 ? " min" : " mins") : "just now";
        return verb + " " + (span.equals("just now") ? span : span + " ago");
    }

    private static int dp(MainActivity act, int v) {
        return (int) (v * act.getResources().getDisplayMetrics().density);
    }
}
