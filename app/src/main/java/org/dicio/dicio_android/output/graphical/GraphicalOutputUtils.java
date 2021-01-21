package org.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.LinearLayoutCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.util.ExceptionUtils;

public class GraphicalOutputUtils {

    public static View inflate(final Context context, @LayoutRes final int layout) {
        return LayoutInflater.from(context).inflate(layout, null);
    }

    public static LinearLayoutCompat.LayoutParams getCenteredLayoutParams() {
        final LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        return layoutParams;
    }

    public static TextView buildText(final Context context,
                                     final CharSequence text,
                                     @DimenRes final int size) {
        final TextView header = new TextView(context);
        header.setLayoutParams(getCenteredLayoutParams());
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(size));
        header.setText(text);
        return header;
    }

    public static TextView buildHeader(final Context context, final CharSequence text) {
        return buildText(context, text, R.dimen.outputHeaderTextSize);
    }

    public static TextView buildSubHeader(final Context context, final CharSequence text) {
        return buildText(context, text, R.dimen.outputSubHeaderTextSize);
    }

    public static TextView buildDescription(final Context context, final CharSequence text) {
        return buildText(context, text, R.dimen.outputDescriptionTextSize);
    }

    public static LinearLayout buildContainer(final Context context, final Drawable divider) {
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(getCenteredLayoutParams());
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setDividerDrawable(divider);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        return linearLayout;
    }

    public static LinearLayout buildContainer(final Context context,
                                              final Drawable divider,
                                              final View... views) {
        final LinearLayout linearLayout = buildContainer(context, divider);
        for (final View view : views) {
            linearLayout.addView(view);
        }
        return linearLayout;
    }


    public static View buildNetworkErrorMessage(final Context context) {
        return buildContainer(context,
                AppCompatResources.getDrawable(context, R.drawable.divider_items),
                buildHeader(context, context.getString(R.string.eval_network_error)),
                buildDescription(context, context.getString(R.string.eval_network_error_description)));
    }

    public static View buildErrorMessage(final Context context, final Throwable throwable) {
        return buildContainer(context,
                AppCompatResources.getDrawable(context, R.drawable.divider_items),
                buildHeader(context, throwable.getMessage()),
                buildDescription(context, ExceptionUtils.getStackTraceString(throwable)));
    }
}
