package com.dicio.dicio_android.renderer;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dicio.component.output.views.DescribedImage;
import com.dicio.dicio_android.R;
import com.dicio.dicio_android.util.ImageLoader;

public class DescribedImageView extends FrameLayout {
    private ImageView image;
    private TextView header;
    private HtmlTextView description;

    public DescribedImageView(Context context) {
        super(context);
        inflate(context, R.layout.output_described_image, this);

        image = findViewById(R.id.image);
        header = findViewById(R.id.header);
        description = findViewById(R.id.description);
    }

    void customize(final DescribedImage data) throws NoSuchFieldException, IllegalAccessException {
        ImageLoader.loadIntoImage(data.getImageSource(), data.getImageSourceType(), image);
        header.setText(data.getHeaderText());
        description.setHtmlText(data.getDescriptionText());

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                data.onClick();
            }
        };
        setOnClickListener(listener);
        description.setOnClickListener(listener);
    }
}
