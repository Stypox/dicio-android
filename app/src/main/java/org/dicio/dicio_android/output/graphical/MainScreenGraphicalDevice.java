package org.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import org.dicio.dicio_android.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainScreenGraphicalDevice implements GraphicalOutputDevice {
    private final LinearLayout outputLayout;
    private final Context context;

    public MainScreenGraphicalDevice(final LinearLayout outputLayout) {
        this.outputLayout = outputLayout;
        this.context = outputLayout.getContext();
    }

    @Override
    public void display(@NonNull final View graphicalOutput, final boolean addDivider) {
        final OutputContainerView outputContainer = new OutputContainerView(context);
        outputContainer.setContent(graphicalOutput);
        outputContainer.setShowDividerBottom(addDivider);
        outputLayout.addView(outputContainer);

        graphicalOutput.post(() ->
                outputLayout.requestChildFocus(outputContainer, outputContainer));
    }
}
