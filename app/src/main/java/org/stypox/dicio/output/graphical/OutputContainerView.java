package org.stypox.dicio.output.graphical;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import org.stypox.dicio.R;
import org.stypox.dicio.util.ThemeUtils;

public class OutputContainerView extends CardView {

    private float dp(final int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, getResources().getDisplayMetrics());
    }

    public OutputContainerView(final Context context) {
        super(context);
        setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setCardElevation(dp(3));
        final float dp10 = dp(10);
        final int dp10int = (int) dp10;

        setContentPadding(dp10int, dp10int, dp10int, dp10int);
        setUseCompatPadding(true);
        setRadius(dp10);
        setCardBackgroundColor(ThemeUtils.resolveColorFromAttr(context, R.attr.cardForeground));
        setPreventCornerOverlap(true);
    }

    public void setContent(final View view) {
        view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(view);

        if (view.isClickable() || view.isLongClickable()) {
            setFocusable(true);
            setOnTouchListener((v, event) -> view.onTouchEvent(event));
        }
    }
}
