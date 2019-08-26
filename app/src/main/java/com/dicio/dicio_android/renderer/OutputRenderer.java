package com.dicio.dicio_android.renderer;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.DescribedImage;
import com.dicio.component.output.views.Description;
import com.dicio.component.output.views.Header;
import com.dicio.component.output.views.Image;
import com.dicio.dicio_android.R;

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
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        layout.setDividerDrawable(context.getResources().getDrawable(R.drawable.output_list_inner_divider));

        List<BaseView> allViews = component.getGraphicalOutput();
        for (BaseView view : allViews) {
            if (view instanceof Header) {
                layout.addView(renderHeader((Header) view, context));
            } else if (view instanceof Description) {
                layout.addView(renderDescription((Description) view, context));
            } else if (view instanceof Image) {
                layout.addView(renderImage((Image) view, context));
            } else if (view instanceof DescribedImage) {
                layout.addView(renderDescribedImage((DescribedImage) view, context));
            }
        }

        CardView result = new CardView(context);
        result.addView(layout);
        result.setCardElevation(context.getResources().getDimension(R.dimen.outputCardElevation));

        int padding = (int)context.getResources().getDimension(R.dimen.outputListPadding);
        result.setRadius(padding);
        result.setContentPadding(padding, padding, padding, padding);
        result.setUseCompatPadding(true);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.cardForeground, typedValue, true);
        result.setCardBackgroundColor(context.getResources().getColor(typedValue.resourceId));

        return result;
    }
}
