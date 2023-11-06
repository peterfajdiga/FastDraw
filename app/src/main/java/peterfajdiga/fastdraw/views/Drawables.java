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

    private static Drawable createHeaderBackgroundCollapsed(
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

        return createBorderedDrawable(
            res,
            gradientDrawable,
            divider && !dividerBottom,
            divider && dividerBottom
        );
    }

    private static Drawable createHeaderBackgroundExpanded(
        @NonNull final Resources res,
        @ColorInt final int color,
        final boolean dividers
    ) {
        return createBorderedDrawable(
            res,
            new ColorDrawable(color),
            dividers,
            dividers
        );
    }

    private static Drawable createBorderedDrawable(
        @NonNull final Resources res,
        final Drawable content,
        final boolean topBorder,
        final boolean bottomBorder
    ) {
        final int borderCount = (topBorder ? 1 : 0) + (bottomBorder ? 1 : 0);
        if (borderCount == 0) {
            return content;
        }

        @ColorInt final int borderColor = res.getColor(R.color.headerDividerColor);
        final int borderThickness = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, res.getDisplayMetrics()));

        final Drawable[] layers = new Drawable[borderCount+1];
        layers[0] = content;
        final int topBorderIndex = 1;
        final int bottomBorderIndex = borderCount;
        if (topBorder) {
            layers[topBorderIndex] = new ColorDrawable(borderColor);
        }
        if (bottomBorder) {
            layers[bottomBorderIndex] = new ColorDrawable(borderColor);
        }

        final LayerDrawable layerDrawable = new LayerDrawable(layers);
        if (topBorder) {
            layerDrawable.setLayerHeight(topBorderIndex, borderThickness);
            layerDrawable.setLayerGravity(topBorderIndex, Gravity.TOP);
        }
        if (bottomBorder) {
            layerDrawable.setLayerHeight(bottomBorderIndex, borderThickness);
            layerDrawable.setLayerGravity(bottomBorderIndex, Gravity.BOTTOM);
        }
        return layerDrawable;
    }
}
