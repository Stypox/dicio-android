package com.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.dicio.dicio_android.R;
import com.dicio.dicio_android.util.ExceptionUtils;

public class GraphicalOutputUtils {


    public static View inflate(Context context, @LayoutRes int layout) {
        return LayoutInflater.from(context).inflate(layout, null);
    }

    public static LinearLayoutCompat.LayoutParams getCenteredLayoutParams() {
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        layoutParams.gravity = Gravity.CENTER;
        return layoutParams;
    }

    public static TextView buildText(Context context, String text, @DimenRes int size) {
        TextView header = new TextView(context);
        header.setLayoutParams(getCenteredLayoutParams());
        header.setGravity(Gravity.CENTER);
        header.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(size));
        header.setText(text);
        return header;
    }

    public static TextView buildHeader(Context context, String text) {
        return buildText(context, text, R.dimen.outputHeaderTextSize);
    }

    public static TextView buildDescription(Context context, String text) {
        return buildText(context, text, R.dimen.outputDescriptionTextSize);
    }

    public static LinearLayout buildContainer(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(getCenteredLayoutParams());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        return linearLayout;
    }

    public static LinearLayout buildContainer(Context context, View... views) {
        LinearLayout linearLayout = buildContainer(context);
        for (View view : views) {
            linearLayout.addView(view);
        }
        return linearLayout;
    }


    public static View buildNetworkErrorMessage(Context context) {
        return buildContainer(context,
                buildHeader(context, context.getString(R.string.eval_header_network_error)),
                buildDescription(context, context.getString(R.string.eval_description_network_error)));
    }

    public static View buildErrorMessage(Context context, Throwable throwable) {
        return buildContainer(context,
                buildHeader(context, throwable.getMessage()),
                buildDescription(context, ExceptionUtils.getStackTraceString(throwable)));
    }
}
