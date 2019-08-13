package com.dicio.dicio_android.renderer;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

public class HtmlTextView extends AppCompatTextView {
    public HtmlTextView(Context context) {
        super(context);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        MovementMethod movementMethod = getMovementMethod();

        boolean shouldContinue = movementMethod.onTouchEvent(this, (Spannable) getText(), event);
        if (shouldContinue && event.getAction() == MotionEvent.ACTION_DOWN) {
            event.setAction(MotionEvent.ACTION_UP);
            movementMethod.onTouchEvent(this, (Spannable) getText(), event);
            event.setAction(MotionEvent.ACTION_DOWN);
        }

        return false;
    }

    public void setHtmlText(String htmlText) {
        super.setText(Html.fromHtml(htmlText));
    }
}
