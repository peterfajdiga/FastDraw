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
        final boolean faded,
        final boolean separators
    ) {
        final TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
            createHeaderBackgroundCollapsed(res, collapsedColor, faded),
            createHeaderBackgroundExpanded(res, expandedColor, separators),
        });
        transitionDrawable.setCrossFadeEnabled(true);
        return transitionDrawable;
    }

    public static Drawable createHeaderBackgroundCollapsed(
        @NonNull final Resources res,
        @ColorInt final int color,
        final boolean faded
    ) {
        if (faded) {
            return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{
                Color.TRANSPARENT,
                color,
            });
        } else {
            return new ColorDrawable(color);
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
                new ColorDrawable(Color.WHITE),
                new ColorDrawable(Color.WHITE),
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

    public static Drawable createScrimBackground(
        @NonNull final Resources res,
        @ColorInt final int color,
        final boolean withBottom
    ) {
        final int[] gradientColors = new int[]{
            Color.TRANSPARENT,
            color,
        };

        final int scrimHeight = Math.round(res.getDimension(R.dimen.system_bar_scrim_height));

        final LayerDrawable layers = new LayerDrawable(new Drawable[]{
            new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColors),
        });
        layers.setLayerHeight(0, scrimHeight);
        layers.setLayerGravity(0, Gravity.TOP);

        if (withBottom) {
            layers.addLayer(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors));
            layers.setLayerHeight(1, scrimHeight);
            layers.setLayerGravity(1, Gravity.BOTTOM);
        }

        return layers;
    }
}
