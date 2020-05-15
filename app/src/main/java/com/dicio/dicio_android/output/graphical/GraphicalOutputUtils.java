package com.dicio.dicio_android.output.graphical;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.dicio.dicio_android.R;

public class GraphicalOutputUtils {

    public static View inflate(Context context, @LayoutRes int layout) {
        return LayoutInflater.from(context).inflate(layout, null);
    }

    public static View buildHeader(Context context, String text) {
        TextView header = new TextView(context);
        header.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        header.setGravity(Gravity.CENTER);
        header.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.outputHeaderTextSize));
        header.setText(text);
        return header;
    }
}
