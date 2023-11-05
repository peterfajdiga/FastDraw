package peterfajdiga.fastdraw.views;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.R;

public class Drawables {
    private Drawables() {}

    public static Drawable createHeaderBackground(
        @NonNull final Resources res,
        @ColorInt final int collapsedColor,
        @ColorInt final int expandedColor,
        final boolean bgGradient,
        final boolean separators
    ) {
        final TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
            createHeaderBackgroundCollapsed(collapsedColor, bgGradient),
            createHeaderBackgroundExpanded(res, expandedColor, separators),
        });
        transitionDrawable.setCrossFadeEnabled(true);
        return transitionDrawable;
    }

    public static Drawable createHeaderBackgroundCollapsed(
        @ColorInt final int color,
        final boolean bgGradient
    ) {
        if (bgGradient) {
            return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{
                Color.TRANSPARENT,
                color,
            });
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    public static Drawable createHeaderBackgroundExpanded(
        @NonNull final Resources res,
        @ColorInt final int color,
        final boolean separators
    ) {
        if (separators) {
            final LayerDrawable layers = new LayerDrawable(new Drawable[]{
                new ColorDrawable(color),
                new ColorDrawable(res.getColor(R.color.separatorColor)),
                new ColorDrawable(res.getColor(R.color.separatorColor)),
            });
            final int separatorHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, res.getDisplayMetrics()));
            layers.setLayerHeight(1, separatorHeight);
            layers.setLayerGravity(1, Gravity.TOP);
            layers.setLayerHeight(2, separatorHeight);
            layers.setLayerGravity(2, Gravity.BOTTOM);
            return layers;
        } else {
            return new ColorDrawable(color);
        }
    }

    public static Drawable createBgGradientDrawable(
        @ColorInt final int color,
        final int heightTop,
        final int heightBottom
    ) {
        final int[] gradientColors = new int[]{
            Color.TRANSPARENT,
            color,
        };

        final LayerDrawable layers = new LayerDrawable(new Drawable[0]);

        if (heightTop > 0) {
            final int index = layers.getNumberOfLayers();
            layers.addLayer(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColors));
            layers.setLayerHeight(index, heightTop);
            layers.setLayerGravity(index, Gravity.TOP);
        }

        if (heightBottom > 0) {
            final int index = layers.getNumberOfLayers();
            layers.addLayer(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors));
            layers.setLayerHeight(index, heightBottom);
            layers.setLayerGravity(index, Gravity.BOTTOM);
        }

        return layers;
    }
}
