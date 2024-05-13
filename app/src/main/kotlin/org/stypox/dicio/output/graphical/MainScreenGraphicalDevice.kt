package org.stypox.dicio.output.graphical

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.LinearLayoutCompat
import org.stypox.dicio.R

class MainScreenGraphicalDevice(
    private var outputScrollView: ScrollView,
    private var outputLayout: LinearLayout
) : GraphicalOutputDevice {
    private var context: Context = outputScrollView.context
    private var atLeastOnePermanentViewDisplayed = false
    private var lastViewWasTemporary = false
    private var pendingDividers = 0
    private var previousScrollY = 0

    init {

        // remove children that were there before, should happen only after opening settings
        outputLayout.removeAllViews()
    }

    override fun display(graphicalOutput: View) {
        displayView(graphicalOutput)
        atLeastOnePermanentViewDisplayed = true
    }

    override fun displayTemporary(graphicalOutput: View) {
        displayView(graphicalOutput)
        lastViewWasTemporary = true // reset in removeTemporaryView
    }

    override fun removeTemporary() {
        removeTemporaryView()
        popDividersAtEnd()
    }

    override fun addDivider() {
        if (atLeastOnePermanentViewDisplayed) {
            // do not add a divider as the first item
            ++pendingDividers
        }
    }

    override fun cleanup() {
    }

    private fun displayView(graphicalOutput: View) {
        removeTemporaryView()
        // TODO verify if this is a good way to detect new conversation blocks being started
        val addedSomeDividers = addPendingDividers()
        val outputContainer = OutputContainerView(context)
        outputContainer.setContent(graphicalOutput)
        outputLayout.addView(outputContainer)

        // scroll to the newly added view, and to the bottom as much as possible
        graphicalOutput.post {
            if (addedSomeDividers) {
                // this is a new conversation: scroll to it even if it hides previous views
                previousScrollY = (outputContainer.y - outputLayout.y).toInt()
            } // otherwise scroll to the first view of this conversation
            outputScrollView.smoothScrollTo(0, previousScrollY)
        }
    }

    private fun addPendingDividers(): Boolean {
        val addedSomeDividers = pendingDividers > 0
        while (pendingDividers > 0) {
            val dividerView = View(context)
            dividerView.layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, context.resources
                    .getDimension(R.dimen.dividerHeightOutputContainers).toInt()
            )
            dividerView.tag = DIVIDER_VIEW_TAG
            outputLayout.addView(dividerView)
            --pendingDividers
        }
        return addedSomeDividers
    }

    private fun removeTemporaryView() {
        if (lastViewWasTemporary) {
            lastViewWasTemporary = false
            if (outputLayout.childCount > 0) {
                val indexOfLastChild = outputLayout.childCount - 1
                val lastChild = outputLayout.getChildAt(indexOfLastChild)
                if (lastChild is ViewGroup) {
                    // this should always be the case. Cleanup so that views inside output container
                    // can be added again to another parent if needed (i.e. they can be reused)
                    lastChild.removeAllViews()
                }
                outputLayout.removeViewAt(indexOfLastChild)
            }
        }
    }

    private fun popDividersAtEnd() {
        // remove dividers above it after the temporary view was removed
        while (outputLayout.childCount > 0) {
            val indexOfLastChild = outputLayout.childCount - 1
            val lastChild = outputLayout.getChildAt(indexOfLastChild)
            if (DIVIDER_VIEW_TAG == lastChild.tag) {
                outputLayout.removeViewAt(indexOfLastChild)
                ++pendingDividers // so that they will be re-added later
            } else {
                break
            }
        }
    }

    companion object {
        private const val DIVIDER_VIEW_TAG = "diVIdeR"
    }
}
