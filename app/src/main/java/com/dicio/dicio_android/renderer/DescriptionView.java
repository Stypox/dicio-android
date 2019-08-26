package com.dicio.dicio_android.renderer;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.widget.FrameLayout;

import com.dicio.component.output.views.Description;
import com.dicio.dicio_android.R;

public class DescriptionView extends FrameLayout {
    HtmlTextView description;

    public DescriptionView(Context context) {
        super(context);
        inflate(context, R.layout.output_description, this);

        description = findViewById(R.id.description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
    }

    void customize(final Description data) {
        description.setHtmlText(data.getText());
    }
}
