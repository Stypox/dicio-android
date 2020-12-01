package org.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.util.ThemeUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainScreenGraphicalDevice implements GraphicalOutputDevice {
    private final LinearLayout outputLayout;
    private final Context context;

    public MainScreenGraphicalDevice(final LinearLayout outputLayout) {
        this.outputLayout = outputLayout;
        this.context = outputLayout.getContext();
    }

    @Override
    public void display(@NonNull final View graphicalOutput) {
        final OutputContainerView outputContainer = new OutputContainerView(context);
        outputContainer.setContent(graphicalOutput);
        outputLayout.addView(outputContainer);

        graphicalOutput.post(() ->
                outputLayout.requestChildFocus(outputContainer, outputContainer));
    }

    @Override
    public void addDivider() {
        if (outputLayout.getChildCount() == 0) {
            // do not add a divider as the first item
            return;
        }

        final View dividerView = new View(context);
        dividerView.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) context.getResources()
                        .getDimension(R.dimen.dividerHeightOutputContainers)));
        outputLayout.addView(dividerView);
    }
}
