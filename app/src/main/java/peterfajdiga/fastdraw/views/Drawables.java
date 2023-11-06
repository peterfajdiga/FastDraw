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
        final boolean dividers,
        final boolean dividerBottom
    ) {
        final TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
            createHeaderBackgroundCollapsed(res, collapsedColor, bgGradient, dividers, dividerBottom),
            createHeaderBackgroundExpanded(res, expandedColor, dividers),
        });
        transitionDrawable.setCrossFadeEnabled(true);
        return transitionDrawable;
    }

    public static Drawable createHeaderBackgroundCollapsed(
        @NonNull final Resources res,
        @ColorInt final int color,
        final boolean bgGradient,
        final boolean divider,
        final boolean dividerBottom
    ) {
        final Drawable gradientDrawable;
        if (bgGradient) {
            gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{
                Color.TRANSPARENT,
                color,
            });
        } else {
            gradientDrawable = new ColorDrawable(Color.TRANSPARENT);
        }

        if (divider) {
            final LayerDrawable layers = new LayerDrawable(new Drawable[]{
                gradientDrawable,
                new ColorDrawable(res.getColor(R.color.headerDividerColor)),
            });
            final int dividerHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, res.getDisplayMetrics()));
            layers.setLayerHeight(1, dividerHeight);
            if (dividerBottom) {
                layers.setLayerGravity(1, Gravity.BOTTOM);
            } else {
                layers.setLayerGravity(1, Gravity.TOP);
            }
            return layers;
        } else {
            return gradientDrawable;
        }
    }

    public static Drawable createHeaderBackgroundExpanded(
        @NonNull final Resources res,
        @ColorInt final int color,
        final boolean dividers
    ) {
        if (dividers) {
            final LayerDrawable layers = new LayerDrawable(new Drawable[]{
                new ColorDrawable(color),
                new ColorDrawable(res.getColor(R.color.headerDividerColor)),
                new ColorDrawable(res.getColor(R.color.headerDividerColor)),
            });
            final int dividerHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, res.getDisplayMetrics()));
            layers.setLayerHeight(1, dividerHeight);
            layers.setLayerGravity(1, Gravity.TOP);
            layers.setLayerHeight(2, dividerHeight);
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
