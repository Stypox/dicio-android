package org.dicio.dicio_android.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.core.content.ContextCompat;

public class ThemeUtils {

    /**
     * Get a resource id from a resource styled according to the context's theme.
     * <p>
     * Taken from NewPipe, file util/ThemeHelper.java, created by @mauriciocolli
     *
     * @param context Android app context
     * @param attr    attribute reference of the resource
     * @return resource ID
     */
    public static int resolveResourceIdFromAttr(final Context context, @AttrRes final int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int attributeResourceId = a.getResourceId(0, 0);
        a.recycle();
        return attributeResourceId;
    }

    /**
     * Get a color from an attr styled according to the context's theme.
     * <p>
     * Taken from NewPipe, file util/ThemeHelper.java, created by @mauriciocolli
     *
     * @param context   Android app context
     * @param attrColor attribute reference of the resource
     * @return the color
     */
    public static int resolveColorFromAttr(final Context context, @AttrRes final int attrColor) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attrColor, value, true);

        if (value.resourceId != 0) {
            return ContextCompat.getColor(context, value.resourceId);
        }

        return value.data;
    }
}
