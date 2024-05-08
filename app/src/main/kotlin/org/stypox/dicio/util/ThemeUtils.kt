package org.stypox.dicio.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import org.stypox.dicio.R

object ThemeUtils {
    /**
     * Get a resource id from a resource styled according to the context's theme.
     *
     * @param context Android app context
     * @param attr    attribute reference of the resource
     * @return resource ID
     * @implNote Taken from NewPipe, file util/ThemeHelper.java, created by @mauriciocolli
     */
    fun resolveResourceIdFromAttr(context: Context, @AttrRes attr: Int): Int {
        val a = context.theme.obtainStyledAttributes(intArrayOf(attr))
        val attributeResourceId = a.getResourceId(0, 0)
        a.recycle()
        return attributeResourceId
    }

    /**
     * Get a color from an attr styled according to the context's theme.
     *
     * @param context   Android app context
     * @param attrColor attribute reference of the resource
     * @return the color
     * @implNote Taken from NewPipe, file util/ThemeHelper.java, created by @mauriciocolli
     */
    fun resolveColorFromAttr(context: Context, @AttrRes attrColor: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attrColor, value, true)
        return if (value.resourceId != 0)
            ContextCompat.getColor(context, value.resourceId)
        else
            value.data
    }

    @StyleRes
    fun chooseThemeBasedOnPreferences(
        context: Context,
        @StyleRes light: Int,
        @StyleRes dark: Int
    ): Int {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.pref_key_theme), "")
        return if (preference == context.getString(R.string.pref_val_theme_dark)) {
            dark
        } else {
            light
        }
    }
}