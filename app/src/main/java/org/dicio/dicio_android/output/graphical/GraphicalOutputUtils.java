package org.dicio.dicio_android.output.graphical;

import static org.dicio.dicio_android.error.UserAction.SKILL_EVALUATION;
import static org.dicio.dicio_android.util.StringUtils.isNullOrEmpty;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.DimenRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.LinearLayoutCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.error.ErrorInfo;
import org.dicio.dicio_android.error.ErrorUtils;
import org.dicio.dicio_android.util.ThemeUtils;

public final class GraphicalOutputUtils {

    private GraphicalOutputUtils() {
    }

    /**
     * Inflates the provided layout using {@code null} as root view.
     * @param context the Android context to use for the layout inflater
     * @param layout the layout resource id of the layout to inflate
     * @return the inflated view
     */
    public static View inflate(final Context context, @LayoutRes final int layout) {
        return LayoutInflater.from(context).inflate(layout, null);
    }

    /**
     * @return the layout parameters to apply to a child of a vertical linear layout in order to
     * make it horizontally centered.
     */
    public static LinearLayoutCompat.LayoutParams getCenteredLinearLayoutParams() {
        final LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        return layoutParams;
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
    public static TextView buildText(final Context context,
                                     final CharSequence text,
                                     @DimenRes final int size,
                                     @AttrRes final int attrColor) {
        final TextView header = new TextView(context);
        header.setLayoutParams(getCenteredLinearLayoutParams());
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(size));
        header.setTextColor(ThemeUtils.resolveColorFromAttr(context, attrColor));
        header.setText(text);
        return header;
    }

    /**
     * Builds a text view with big text, to be used for headers or titles
     * @see #buildText(Context, CharSequence, int, int)
     * @see #buildSubHeader(Context, CharSequence)
     * @see #buildDescription(Context, CharSequence)
     *
     * @param context the Android context to use to initialize the view
     * @param text the content of the text view
     * @return the built view
     */
    public static TextView buildHeader(final Context context, final CharSequence text) {
        return buildText(context, text, R.dimen.outputHeaderTextSize,
                android.R.attr.textColorPrimary);
    }

    /**
     * Builds a text view with medium-sized text, to be used for sub-headers or subtitles
     * @see #buildText(Context, CharSequence, int, int)
     * @see #buildHeader(Context, CharSequence)
     * @see #buildDescription(Context, CharSequence)
     *
     * @param context the Android context to use to initialize the view
     * @param text the content of the text view
     * @return the built view
     */
    public static TextView buildSubHeader(final Context context, final CharSequence text) {
        return buildText(context, text, R.dimen.outputSubHeaderTextSize,
                android.R.attr.textColorPrimary);
    }

    /**
     * Builds a text view with normally-sized text, to be used for long texts, descriptions or
     * captions
     * @see #buildText(Context, CharSequence, int, int)
     * @see #buildHeader(Context, CharSequence)
     * @see #buildSubHeader(Context, CharSequence)
     *
     * @param context the Android context to use to initialize the view
     * @param text the content of the text view
     * @return the built view
     */
    public static TextView buildDescription(final Context context, final CharSequence text) {
        return buildText(context, text, R.dimen.outputDescriptionTextSize,
                android.R.attr.textColorSecondary);
    }

    /**
     * Builds a vertical linear layout that uses the provided divider between each of the added
     * views (see {@link LinearLayout#SHOW_DIVIDER_MIDDLE}).
     * @see #buildVerticalLinearLayout(Context, Drawable, View...)
     *
     * @param context the Android context to use to initialize the view
     * @param divider the drawable to display in between items
     * @return the built view
     */
    public static LinearLayout buildVerticalLinearLayout(final Context context,
                                                         final Drawable divider) {
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(getCenteredLinearLayoutParams());
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setDividerDrawable(divider);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        return linearLayout;
    }

    /**
     * Builds a vertical linear layout that uses the provided divider between each of the added
     * views (see {@link LinearLayout#SHOW_DIVIDER_MIDDLE}).
     * @see #buildVerticalLinearLayout(Context, Drawable)
     *
     * @param context the Android context to use to initialize the view
     * @param divider the drawable to display in between items
     * @param views the children to add, in order, to the layout
     * @return the built view
     */
    public static LinearLayout buildVerticalLinearLayout(final Context context,
                                                         final Drawable divider,
                                                         final View... views) {
        final LinearLayout linearLayout = buildVerticalLinearLayout(context, divider);
        for (final View view : views) {
            linearLayout.addView(view);
        }
        return linearLayout;
    }

    /**
     * Builds a view explaining that a network error has occoured
     * @see #buildErrorMessage(Context, Throwable)
     *
     * @param context the Android context to use to initialize the view
     * @return the built view
     */
    public static View buildNetworkErrorMessage(final Context context) {
        return buildVerticalLinearLayout(context,
                AppCompatResources.getDrawable(context, R.drawable.divider_items),
                buildHeader(context, context.getString(R.string.eval_network_error)),
                buildDescription(context,
                        context.getString(R.string.eval_network_error_description)));
    }

    /**
     * Builds a view explaining that an error has occoured, containing the {@code throwable}'s
     * message as title and a button that allows reporting it by opening the error activity with
     * {@link ErrorUtils#openActivity(Context, ErrorInfo)}
     * @see #buildNetworkErrorMessage(Context)
     *
     * @param context the Android context to use to initialize the view
     * @param throwable the exception to show information about and possibly report
     * @return the built view
     */
    public static View buildErrorMessage(final Context context, final Throwable throwable) {
        final View view = inflate(context, R.layout.error_panel);

        String description = throwable.getLocalizedMessage();
        if (isNullOrEmpty(description)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                description = throwable.getClass().getTypeName();
            } else {
                description = throwable.getClass().getSimpleName();
            }
        }
        ((TextView) view.findViewById(R.id.descriptionTextView)).setText(description);

        view.findViewById(R.id.reportTextView).setOnClickListener(v ->
                ErrorUtils.openActivity(context, new ErrorInfo(throwable, SKILL_EVALUATION)));

        return view;
    }
}
