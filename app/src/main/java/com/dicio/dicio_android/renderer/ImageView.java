package com.dicio.dicio_android.renderer;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.dicio.component.output.views.Image;
import com.dicio.dicio_android.R;
import com.dicio.dicio_android.util.ImageLoader;

public class ImageView extends FrameLayout {
    android.widget.ImageView image;

    public ImageView(Context context) {
        super(context);
        inflate(context, R.layout.output_image, this);

        image = findViewById(R.id.image);
    }

    void customize(final Image data) throws NoSuchFieldException, IllegalAccessException {
        ImageLoader.loadIntoImage(data.getSource(), data.getSourceType(), image);
        image.setOnClickListener(v -> data.onClick());
    }
}
