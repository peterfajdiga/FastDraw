package peterfajdiga.fastdraw;

import android.app.WallpaperColors;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WallpaperColorUtils {
    private WallpaperColorUtils() {}

    @ColorInt
    public static int getDarkColor(@Nullable final WallpaperColors wallpaperColors) {
        if (wallpaperColors != null) {
            final Color primary = wallpaperColors.getPrimaryColor();
            if (isColorDark(primary)) {
                return primary.toArgb();
            }

            final Color secondary = wallpaperColors.getSecondaryColor();
            if (isColorDark(secondary)) {
                return secondary.toArgb();
            }

            final Color tertiary = wallpaperColors.getTertiaryColor();
            if (isColorDark(tertiary)) {
                return tertiary.toArgb();
            }
        }

        return Color.BLACK;
    }

    @ColorInt
    public static int getDarkAccentColor(@Nullable final WallpaperColors wallpaperColors, @NonNull final Resources resources) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return resources.getColor(android.R.color.system_accent2_600);
        }

        if (wallpaperColors != null) {
            final Color secondary = wallpaperColors.getSecondaryColor();
            if (isColorDark(secondary)) {
                return secondary.toArgb();
            }

            final Color primary = wallpaperColors.getPrimaryColor();
            if (isColorDark(primary)) {
                return primary.toArgb();
            }

            final Color tertiary = wallpaperColors.getTertiaryColor();
            if (isColorDark(tertiary)) {
                return tertiary.toArgb();
            }
        }

        return Color.BLACK;
    }

    private static boolean isColorDark(@Nullable final Color color) {
        return color != null && color.luminance() < 0.5f;
    }
}
