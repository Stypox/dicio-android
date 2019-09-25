package com.dicio.dicio_android.io.graphical;

import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import com.dicio.dicio_android.io.graphical.render.OutputContainerView;

public class MainScreenGraphicalDevice implements GraphicalOutputDevice {
    private LinearLayout outputLayout;
    private ScrollView outputScrollView;

    public MainScreenGraphicalDevice(LinearLayout outputLayout, ScrollView outputScrollView) {
        this.outputLayout = outputLayout;
        this.outputScrollView = outputScrollView;
    }

    @Override
    public void display(@NonNull OutputContainerView graphicalOutput) {
        outputLayout.addView(graphicalOutput);
        outputScrollView.post(() -> outputScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
