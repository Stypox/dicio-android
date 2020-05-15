package com.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.dicio.dicio_android.R;

public class OutputContainerView extends View {
    CardView baseView;

    public OutputContainerView(Context context) {
        super(context);
        inflate(context, R.layout.output_container, null);
        baseView = findViewById(R.id.outputContainerView);
    }
    
    public void setContent(View view) {
        view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        baseView.addView(view);
    }
}
