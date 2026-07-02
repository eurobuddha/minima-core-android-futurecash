package org.minimarex.futurecash;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** About tab — FutureCash intro + entry into the Send / Future flows. */
public class AboutView extends BaseView {

    private final LinearLayout container;

    public AboutView(MainActivity a) {
        super(a, R.layout.view_page);
        container = find(R.id.pageContainer);
        build();
    }

    @Override public void refresh() {}

    private void build() {
        container.removeAllViews();

        TextView title = new TextView(act);
        title.setText("FutureCash");
        title.setTextColor(FcDesign.TEXT);
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        container.addView(title);

        TextView blurb = new TextView(act);
        blurb.setText("Send Minima or tokens to the future. Funds are locked in an on-chain contract until "
                + "a block you choose — then the recipient can collect them. Lock savings for yourself or "
                + "schedule a payment to someone else.");
        blurb.setTextColor(FcDesign.DIM);
        blurb.setTextSize(15f);
        blurb.setLineSpacing(0, 1.3f);
        blurb.setPadding(0, dp(10), 0, dp(28));
        container.addView(blurb);

        container.addView(cta("Send to the future", true, v -> act.goToTab(MainActivity.TAB_SEND)));
        container.addView(cta("View future payments", false, v -> act.goToTab(MainActivity.TAB_FUTURE)));

        TextView foot = new TextView(act);
        foot.setText("FutureCash is an on-chain Minima smart contract — fully interoperable with the FutureCash MiniDapp.");
        foot.setTextColor(FcDesign.DIM_2);
        foot.setTextSize(11f);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, dp(28), 0, 0);
        container.addView(foot);
    }

    private Button cta(String text, boolean primary, android.view.View.OnClickListener onClick) {
        Button b = new Button(act);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(15f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? FcDesign.YELLOW : FcDesign.BLACK));
        b.setTextColor(primary ? FcDesign.ON_YELLOW : FcDesign.TEXT_ON_DARK);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        b.setLayoutParams(lp);
        b.setOnClickListener(onClick);
        return b;
    }
}
