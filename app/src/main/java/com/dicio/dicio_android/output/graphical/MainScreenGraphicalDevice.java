package com.dicio.dicio_android.output.graphical;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

public class MainScreenGraphicalDevice implements GraphicalOutputDevice {
    private LinearLayout outputLayout;
    private ScrollView outputScrollView;

    public MainScreenGraphicalDevice(LinearLayout outputLayout, ScrollView outputScrollView) {
        this.outputLayout = outputLayout;
        this.outputScrollView = outputScrollView;
    }

    @Override
    public void display(@NonNull View graphicalOutput) {
        OutputContainerView outputContainer = new OutputContainerView(outputLayout.getContext());
        outputContainer.setContent(graphicalOutput);
        outputLayout.addView(outputContainer);
        outputScrollView.post(() -> outputScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
