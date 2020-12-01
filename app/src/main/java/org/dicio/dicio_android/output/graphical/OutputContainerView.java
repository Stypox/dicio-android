package org.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.util.ThemeUtils;

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

        setShowDividerBottom(false);
        setUseCompatPadding(true);
        setRadius(dp(10));
        setCardBackgroundColor(ThemeUtils.resolveColorFromAttr(context, R.attr.cardForeground));
        setPreventCornerOverlap(true);
    }
    
    public void setContent(final View view) {
        view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(view);
    }

    public void setShowDividerBottom(final boolean showDividerBottom) {
        int dp10int = (int) dp(10);
        if (showDividerBottom) {
            setContentPadding(dp10int, dp10int, dp10int, dp10int + (int) dp(40));
        } else {
            setContentPadding(dp10int, dp10int, dp10int, dp10int);
        }
    }
}
