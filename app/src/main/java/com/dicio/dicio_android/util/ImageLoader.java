package com.dicio.dicio_android.util;

import android.widget.ImageView;

import com.dicio.component.output.views.Image;
import com.dicio.dicio_android.R;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;

public class ImageLoader {
    public static void loadIntoImage(String source, Image.SourceType sourceType, ImageView view) throws NoSuchFieldException, IllegalAccessException {
        switch (sourceType) {
            case url: case local:
                Picasso.get().load(source).into(view);
                break;
            case other:
                Field idField = R.drawable.class.getDeclaredField(source);
                view.setImageResource(idField.getInt(idField));
                break;
        }
    }
}
