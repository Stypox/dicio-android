package com.dicio.dicio_android.renderer;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.dicio.dicio_android.R;

public class OutputContainerView extends FrameLayout {
    LinearLayout layout;

    public OutputContainerView(Context context) {
        super(context);
        inflate(context, R.layout.output_container, this);
        layout = findViewById(R.id.layout);
    }

    @Override
    public void addView(View view) {
        layout.addView(view);
    }
}
