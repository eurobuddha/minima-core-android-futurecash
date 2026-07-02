package org.minimarex.futurecash;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Send tab — create a future payment + your existing payments. */
public class SendView extends BaseView {

    private final LinearLayout container;

    public SendView(MainActivity a) {
        super(a, R.layout.view_page);
        container = find(R.id.pageContainer);
        refresh();
    }

    @Override public void onShown() { act.requestReload(); refresh(); }

    @Override
    public void refresh() {
        container.removeAllViews();

        container.addView(button("＋  Send to the future", true, v -> {
            Intent i = new Intent(act, CreateFutureActivity.class);
            i.putExtra("script", act.scriptAddress());
            act.startActivity(i);
        }));

        TextView h = new TextView(act);
        h.setText("My future payments");
        h.setTextColor(FcDesign.TEXT);
        h.setTextSize(16f);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        h.setPadding(0, dp(20), 0, dp(12));
        container.addView(h);

        List<FuturePayment> payments = act.payments();
        if (payments.isEmpty()) {
            container.addView(empty("Nothing locked yet. Send Minima or a token to a future block."));
        } else {
            for (FuturePayment p : payments) container.addView(FcCardUi.card(act, p));
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

    private View button(String text, boolean primary, View.OnClickListener onClick) {
        TextView b = new TextView(act);
        b.setText(text);
        b.setTextColor(primary ? FcDesign.ON_YELLOW : FcDesign.TEXT_ON_DARK);
        b.setTextSize(15f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(0, dp(14), 0, dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(primary ? FcDesign.YELLOW : FcDesign.BLACK);
        bg.setCornerRadius(dp(8));
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        b.setLayoutParams(lp);
        b.setOnClickListener(onClick);
        return b;
    }
}
