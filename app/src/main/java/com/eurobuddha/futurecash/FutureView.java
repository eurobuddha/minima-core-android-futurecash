package com.eurobuddha.futurecash;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Future tab — your locked payments split into Ready (collectable now) and Pending (still locked). */
public class FutureView extends BaseView {

    private final LinearLayout container;

    public FutureView(MainActivity a) {
        super(a, R.layout.view_page);
        container = find(R.id.pageContainer);
        refresh();
    }

    @Override public void onShown() { act.requestReload(); refresh(); }

    @Override
    public void refresh() {
        container.removeAllViews();

        TextView h = new TextView(act);
        h.setText("Future");
        h.setTextColor(FcDesign.TEXT);
        h.setTextSize(22f);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        h.setPadding(0, 0, 0, dp(6));
        container.addView(h);

        TextView blurb = new TextView(act);
        blurb.setText("Money locked until a future block. Tap a ready payment to collect it to the recipient.");
        blurb.setTextColor(FcDesign.DIM);
        blurb.setTextSize(14f);
        blurb.setPadding(0, 0, 0, dp(16));
        container.addView(blurb);

        int tip = act.chainBlock();
        List<FuturePayment> ready = new ArrayList<>(), pending = new ArrayList<>();
        for (FuturePayment p : act.payments()) (p.matured(tip) ? ready : pending).add(p);

        if (ready.isEmpty() && pending.isEmpty()) {
            container.addView(empty("No future payments yet."));
            return;
        }
        if (!ready.isEmpty()) {
            container.addView(section("Ready to collect"));
            for (FuturePayment p : ready) container.addView(FcCardUi.card(act, p));
        }
        if (!pending.isEmpty()) {
            container.addView(section("Pending"));
            for (FuturePayment p : pending) container.addView(FcCardUi.card(act, p));
        }
    }

    private TextView section(String text) {
        TextView t = new TextView(act);
        t.setText(text.toUpperCase());
        t.setTextColor(FcDesign.DIM);
        t.setTextSize(11f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setLetterSpacing(0.08f);
        t.setPadding(dp(2), dp(8), 0, dp(10));
        return t;
    }

    private TextView empty(String text) {
        TextView t = new TextView(act);
        t.setText(text);
        t.setTextColor(FcDesign.DIM);
        t.setTextSize(14f);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(24), dp(8), 0);
        return t;
    }
}
