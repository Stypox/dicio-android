package org.dicio.dicio_android.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;

public final class ThemeUtils {

    private ThemeUtils() {
    }

    /**
     * Get a resource id from a resource styled according to the context's theme.
     *
     * @param context Android app context
     * @param attr    attribute reference of the resource
     * @return resource ID
     * @implNote Taken from NewPipe, file util/ThemeHelper.java, created by @mauriciocolli
     */
    public static int resolveResourceIdFromAttr(final Context context, @AttrRes final int attr) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        final int attributeResourceId = a.getResourceId(0, 0);
        a.recycle();
        return attributeResourceId;
    }

    /**
     * Get a color from an attr styled according to the context's theme.
     *
     * @param context   Android app context
     * @param attrColor attribute reference of the resource
     * @return the color
     * @implNote Taken from NewPipe, file util/ThemeHelper.java, created by @mauriciocolli
     */
    public static int resolveColorFromAttr(final Context context, @AttrRes final int attrColor) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attrColor, value, true);

        if (value.resourceId != 0) {
            return ContextCompat.getColor(context, value.resourceId);
        }

        return value.data;
    }

    @StyleRes
    public static int chooseThemeBasedOnPreferences(final Context context,
                                                    @StyleRes final int light,
                                                    @StyleRes final int dark) {
        final String preference = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_theme), "");

        if (preference.equals(context.getString(R.string.pref_val_theme_dark))) {
            return dark;
        } else {
            return light;
        }
    }
}
