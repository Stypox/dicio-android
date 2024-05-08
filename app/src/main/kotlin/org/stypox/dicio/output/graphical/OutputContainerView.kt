package org.stypox.dicio.output.graphical

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import org.stypox.dicio.R
import org.stypox.dicio.util.ThemeUtils

class OutputContainerView(context: Context) : CardView(context) {
    private fun dp(value: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(), resources.displayMetrics
        )
    }

    init {
        layoutParams = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cardElevation = dp(3)
        val dp10 = dp(10)
        val dp10int = dp10.toInt()
        setContentPadding(dp10int, dp10int, dp10int, dp10int)
        useCompatPadding = true
        radius = dp10
        setCardBackgroundColor(ThemeUtils.resolveColorFromAttr(context, R.attr.cardForeground))
        preventCornerOverlap = true
    }

    fun setContent(view: View) {
        view.layoutParams = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addView(view)
        if (view.isClickable || view.isLongClickable) {
            isFocusable = true
            //noinspection ClickableViewAccessibility
            setOnTouchListener { _, event -> view.onTouchEvent(event) }
        }
    }
}