package com.eurobuddha.futurecash;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Future tab — payments that are NOT collectable yet, soonest to unlock first. Ready ones live on the Send tab. */
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
        blurb.setText("Money still locked, soonest to unlock first. Collectable payments move to the Send tab.");
        blurb.setTextColor(FcDesign.DIM);
        blurb.setTextSize(14f);
        blurb.setPadding(0, 0, 0, dp(16));
        container.addView(blurb);

        // Locked only — the ready ones are the Send tab's list. No Ready/Pending sections here: with a
        // single bucket the unlock-block order carries the whole story.
        List<FuturePayment> pending = act.lockedPayments();
        if (!pending.isEmpty()) {
            for (FuturePayment p : pending) container.addView(FcCardUi.card(act, p));
            return;
        }
        int ready = act.readyPayments().size();
        if (ready > 0) {
            container.addView(empty("Nothing still locked.\n\n"
                    + ready + (ready == 1 ? " payment is" : " payments are") + " ready to collect — see the Send tab."));
        } else {
            container.addView(empty(act.emptyReason()));
        }
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
