package org.dicio.dicio_android.output.graphical;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

public class MainScreenGraphicalDevice implements GraphicalOutputDevice {
    private final LinearLayout outputLayout;

    public MainScreenGraphicalDevice(LinearLayout outputLayout) {
        this.outputLayout = outputLayout;
    }

    @Override
    public void display(@NonNull View graphicalOutput) {
        OutputContainerView outputContainer = new OutputContainerView(outputLayout.getContext());
        outputContainer.setContent(graphicalOutput);
        outputLayout.addView(outputContainer);

        graphicalOutput.post(() ->
                outputLayout.requestChildFocus(outputContainer, outputContainer));
    }
}
