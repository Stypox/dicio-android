package com.dicio.dicio_android.renderer;

import android.content.Context;
import android.view.View;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.DescribedImage;
import com.dicio.component.output.views.Description;
import com.dicio.component.output.views.Header;
import com.dicio.component.output.views.Image;

import java.util.List;

public class OutputRenderer {
    
    private static View renderHeader(Header data, Context context) {
        HeaderView headerView = new HeaderView(context);
        headerView.customize(data);
        return headerView;
    }
    
    private static View renderDescription(Description data, Context context) {
        DescriptionView descriptionView = new DescriptionView(context);
        descriptionView.customize(data);
        return descriptionView;
    }
    
    private static View renderImage(final Image data, Context context) throws IllegalAccessException, NoSuchFieldException {
        ImageView imageView = new ImageView(context);
        imageView.customize(data);
        return imageView;
    }
    
    private static View renderDescribedImage(DescribedImage data, Context context) throws NoSuchFieldException, IllegalAccessException {
        DescribedImageView describedImageView = new DescribedImageView(context);
        describedImageView.customize(data);
        return describedImageView;
    }

    
    public static View renderComponentOutput(AssistanceComponent component, Context context) throws NoSuchFieldException, IllegalAccessException {
        OutputContainerView outputContainerView = new OutputContainerView(context);

        List<BaseView> allViews = component.getGraphicalOutput();
        for (BaseView view : allViews) {
            if (view instanceof Header) {
                outputContainerView.addView(renderHeader((Header) view, context));
            } else if (view instanceof Description) {
                outputContainerView.addView(renderDescription((Description) view, context));
            } else if (view instanceof Image) {
                outputContainerView.addView(renderImage((Image) view, context));
            } else if (view instanceof DescribedImage) {
                outputContainerView.addView(renderDescribedImage((DescribedImage) view, context));
            }
        }

        return outputContainerView;
    }
}
