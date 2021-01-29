package org.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import org.dicio.dicio_android.R;
import org.dicio.skill.output.GraphicalOutputDevice;

public class MainScreenGraphicalDevice implements GraphicalOutputDevice {
    private final LinearLayout outputLayout;
    private final Context context;
    private boolean lastViewWasTemporary = false;

    public MainScreenGraphicalDevice(final LinearLayout outputLayout) {
        this.outputLayout = outputLayout;
        this.context = outputLayout.getContext();
    }

    @Override
    public void display(@NonNull final View graphicalOutput) {
        removeTemporary();

        final OutputContainerView outputContainer = new OutputContainerView(context);
        outputContainer.setContent(graphicalOutput);
        outputLayout.addView(outputContainer);

        graphicalOutput.post(() ->
                outputLayout.requestChildFocus(outputContainer, outputContainer));
    }

    @Override
    public void displayTemporary(@NonNull final View graphicalOutput) {
        display(graphicalOutput);
        lastViewWasTemporary = true;
    }

    @Override
    public void removeTemporary() {
        if (lastViewWasTemporary) {
            lastViewWasTemporary = false;
            if (outputLayout.getChildCount() > 0) {
                final View lastChild = outputLayout.getChildAt(outputLayout.getChildCount() - 1);

                if (lastChild instanceof ViewGroup) {
                    // this should always be the case. Cleanup so that views inside output container
                    // can be added again to another parent if needed (i.e. they can be reused)
                    ((ViewGroup) lastChild).removeAllViews();
                }

                outputLayout.removeView(lastChild);
            }
        }
    }

    @Override
    public void addDivider() {
        removeTemporary();

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
