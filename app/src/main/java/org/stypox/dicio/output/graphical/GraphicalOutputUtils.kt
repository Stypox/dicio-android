package org.stypox.dicio.output.graphical

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.LinearLayoutCompat
import org.stypox.dicio.R
import org.stypox.dicio.error.ErrorInfo
import org.stypox.dicio.error.ErrorUtils
import org.stypox.dicio.error.UserAction
import org.stypox.dicio.util.StringUtils
import org.stypox.dicio.util.ThemeUtils

object GraphicalOutputUtils {
    /**
     * Inflates the provided layout using `null` as root view.
     * @param context the Android context to use for the layout inflater
     * @param layout the layout resource id of the layout to inflate
     * @return the inflated view
     */
    fun inflate(context: Context, @LayoutRes layout: Int): View {
        return LayoutInflater.from(context).inflate(layout, null)
    }

    /**
     * @return the layout parameters to apply to a child of a vertical linear layout in order to
     * make it horizontally centered.
     */
    val centeredLinearLayoutParams: LinearLayoutCompat.LayoutParams
        get() {
            val layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.weight = 1.0f
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
            return layoutParams
        }

    /**
     * Builds a text view
     *
     * @param context the Android context to use to initialize the view
     * @param text the content of the text view
     * @param size the dimension resource id representing the text size
     * @param attrColor the attribute resource id pointing to the themed color to give to the text
     * @return the built view
     */
    fun buildText(
        context: Context,
        text: CharSequence,
        @DimenRes size: Int,
        @AttrRes attrColor: Int
    ): TextView {
        val header = TextView(context)
        header.layoutParams = centeredLinearLayoutParams
        header.gravity = Gravity.CENTER_HORIZONTAL
        header.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(size))
        header.setTextColor(ThemeUtils.resolveColorFromAttr(context, attrColor))
        header.text = text
        return header
    }

    /**
     * Builds a text view with big text, to be used for headers or titles
     * @see buildText
     * @see buildSubHeader
     * @see buildDescription
     * @param context the Android context to use to initialize the view
     * @param text the content of the text view
     * @return the built view
     */
    fun buildHeader(context: Context, text: CharSequence): TextView {
        return buildText(
            context, text, R.dimen.outputHeaderTextSize,
            android.R.attr.textColorPrimary
        )
    }

    /**
     * Builds a text view with medium-sized text, to be used for sub-headers or subtitles
     * @see buildText
     * @see buildHeader
     * @see buildDescription
     * @param context the Android context to use to initialize the view
     * @param text the content of the text view
     * @return the built view
     */
    fun buildSubHeader(context: Context, text: CharSequence): TextView {
        return buildText(
            context, text, R.dimen.outputSubHeaderTextSize,
            android.R.attr.textColorPrimary
        )
    }

    /**
     * Builds a text view with normally-sized text, to be used for long texts, descriptions or
     * captions
     * @see buildText
     * @see buildHeader
     * @see buildSubHeader
     * @param context the Android context to use to initialize the view
     * @param text the content of the text view
     * @return the built view
     */
    fun buildDescription(context: Context, text: CharSequence): TextView {
        return buildText(
            context, text, R.dimen.outputDescriptionTextSize,
            android.R.attr.textColorSecondary
        )
    }

    /**
     * Builds a vertical linear layout that uses the provided divider between each of the added
     * views (see [LinearLayout.SHOW_DIVIDER_MIDDLE]).
     * @see buildVerticalLinearLayout
     * @param context the Android context to use to initialize the view
     * @param divider the drawable to display in between items
     * @return the built view
     */
    fun buildVerticalLinearLayout(
        context: Context?,
        divider: Drawable?
    ): LinearLayout {
        val linearLayout = LinearLayout(context)
        linearLayout.layoutParams = centeredLinearLayoutParams
        linearLayout.gravity = Gravity.CENTER_HORIZONTAL
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.dividerDrawable = divider
        linearLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        return linearLayout
    }

    /**
     * Builds a vertical linear layout that uses the provided divider between each of the added
     * views (see [LinearLayout.SHOW_DIVIDER_MIDDLE]).
     * @see buildVerticalLinearLayout
     * @param context the Android context to use to initialize the view
     * @param divider the drawable to display in between items
     * @param views the children to add, in order, to the layout
     * @return the built view
     */
    fun buildVerticalLinearLayout(
        context: Context?,
        divider: Drawable?,
        vararg views: View?
    ): LinearLayout {
        val linearLayout = buildVerticalLinearLayout(context, divider)
        for (view in views) {
            linearLayout.addView(view)
        }
        return linearLayout
    }

    /**
     * Builds a view explaining that a network error has occoured
     * @see buildErrorMessage
     * @param context the Android context to use to initialize the view
     * @return the built view
     */
    fun buildNetworkErrorMessage(context: Context): View {
        return buildVerticalLinearLayout(
            context,
            AppCompatResources.getDrawable(context, R.drawable.divider_items),
            buildHeader(context, context.getString(R.string.eval_network_error)),
            buildDescription(
                context,
                context.getString(R.string.eval_network_error_description)
            )
        )
    }

    /**
     * Builds a view explaining that an error has occoured, containing the `throwable`'s
     * message as title and a button that allows reporting it by opening the error activity with
     * [ErrorUtils.openActivity]
     * @see buildNetworkErrorMessage
     * @param context the Android context to use to initialize the view
     * @param throwable the exception to show information about and possibly report
     * @return the built view
     */
    fun buildErrorMessage(context: Context, throwable: Throwable): View {
        val view = inflate(context, R.layout.error_panel)
        var description = throwable.localizedMessage
        if (description.isNullOrEmpty()) {
            description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                throwable.javaClass.typeName
            } else {
                throwable.javaClass.simpleName
            }
        }
        (view.findViewById<View>(R.id.descriptionTextView) as TextView).text = description
        view.findViewById<View>(R.id.reportTextView).setOnClickListener {
            ErrorUtils.openActivity(
                context,
                ErrorInfo(throwable, UserAction.SKILL_EVALUATION)
            )
        }
        return view
    }
}