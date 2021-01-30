package org.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import org.dicio.dicio_android.R;
import org.dicio.skill.output.GraphicalOutputDevice;

public class MainScreenGraphicalDevice implements GraphicalOutputDevice {

    private static final String DIVIDER_VIEW_TAG = "diVIdeR";

    private final ScrollView outputScrollView;
    private final LinearLayout outputLayout;
    private final Context context;

    private boolean atLeastOnePermanentViewDisplayed;
    private boolean lastViewWasTemporary = false;
    private int pendingDividers = 0;
    private int previousScrollY = 0;

    public MainScreenGraphicalDevice(final ScrollView outputScrollView,
                                     final LinearLayout outputLayout) {
        this.outputScrollView = outputScrollView;
        this.outputLayout = outputLayout;
        this.context = outputScrollView.getContext();
        atLeastOnePermanentViewDisplayed = outputLayout.getChildCount() > 0;
    }

    @Override
    public void display(@NonNull final View graphicalOutput) {
        displayView(graphicalOutput);
        atLeastOnePermanentViewDisplayed = true;
    }

    @Override
    public void displayTemporary(@NonNull final View graphicalOutput) {
        displayView(graphicalOutput);
        lastViewWasTemporary = true; // reset in removeTemporaryView
    }

    @Override
    public void removeTemporary() {
        removeTemporaryView();
        popDividersAtEnd();
    }

    @Override
    public void addDivider() {
        if (atLeastOnePermanentViewDisplayed) {
            // do not add a divider as the first item
            ++pendingDividers;
        }
    }


    private void displayView(@NonNull final View graphicalOutput) {
        removeTemporaryView();
        // TODO verify if this is a good way to detect new conversation blocks being started
        final boolean addedSomeDividers = addPendingDividers();

        final OutputContainerView outputContainer = new OutputContainerView(context);
        outputContainer.setContent(graphicalOutput);
        outputLayout.addView(outputContainer);

        // scroll to the newly added view, and to the bottom as much as possible
        graphicalOutput.post(() -> {
            if (addedSomeDividers) {
                // this is a new conversation: scroll to it even if it hides previous views
                previousScrollY = (int) (outputContainer.getY() - outputLayout.getY());
            } // otherwise scroll to the first view of this conversation
            outputScrollView.smoothScrollTo(0, previousScrollY);
        });
    }

    private boolean addPendingDividers() {
        final boolean addedSomeDividers = pendingDividers > 0;
        for (; pendingDividers > 0; --pendingDividers) {
            final View dividerView = new View(context);
            dividerView.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) context.getResources()
                            .getDimension(R.dimen.dividerHeightOutputContainers)));
            dividerView.setTag(DIVIDER_VIEW_TAG);
            outputLayout.addView(dividerView);
        }
        return addedSomeDividers;
    }

    private void removeTemporaryView() {
        if (lastViewWasTemporary) {
            lastViewWasTemporary = false;
            if (outputLayout.getChildCount() > 0) {
                final int indexOfLastChild = outputLayout.getChildCount() - 1;
                final View lastChild = outputLayout.getChildAt(indexOfLastChild);

                if (lastChild instanceof ViewGroup) {
                    // this should always be the case. Cleanup so that views inside output container
                    // can be added again to another parent if needed (i.e. they can be reused)
                    ((ViewGroup) lastChild).removeAllViews();
                }

                outputLayout.removeViewAt(indexOfLastChild);
            }
        }
    }

    private void popDividersAtEnd() {
        // remove dividers above it after the temporary view was removed
        while (outputLayout.getChildCount() > 0) {
            final int indexOfLastChild = outputLayout.getChildCount() - 1;
            final View lastChild = outputLayout.getChildAt(indexOfLastChild);

            if (DIVIDER_VIEW_TAG.equals(lastChild.getTag())) {
                outputLayout.removeViewAt(indexOfLastChild);
                ++pendingDividers; // so that they will be re-added later
            } else {
                break;
            }
        }
    }
}
