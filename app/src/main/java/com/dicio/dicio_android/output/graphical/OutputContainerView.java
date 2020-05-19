package com.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.dicio.dicio_android.R;
import com.dicio.dicio_android.util.ThemeUtils;

public class OutputContainerView extends CardView {

    private float dp(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, getResources().getDisplayMetrics());
    }

    public OutputContainerView(Context context) {
        super(context);
        setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setCardElevation(dp(3));
        float dp10 = dp(10);
        int dp10int = (int)dp10;

        setContentPadding(dp10int, dp10int, dp10int, dp10int);
        setUseCompatPadding(true);
        setRadius(dp10);
        setCardBackgroundColor(ThemeUtils.resolveColorFromAttr(context, R.attr.cardForeground));
        setPreventCornerOverlap(true);
    }
    
    public void setContent(View view) {
        view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(view);
    }
}
