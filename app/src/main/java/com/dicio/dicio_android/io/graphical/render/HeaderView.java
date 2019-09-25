package com.dicio.dicio_android.io.graphical.render;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dicio.component.output.views.Header;
import com.dicio.dicio_android.R;

public class HeaderView extends FrameLayout {
    TextView header;

    public HeaderView(Context context) {
        super(context);
        inflate(context, R.layout.output_header, this);

        header = findViewById(R.id.header);
    }

    void customize(final Header data) {
        header.setText(data.getText());
    }
}
