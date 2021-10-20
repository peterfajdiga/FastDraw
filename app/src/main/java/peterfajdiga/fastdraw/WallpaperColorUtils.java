package peterfajdiga.fastdraw;

import android.app.WallpaperColors;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O_MR1)
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
    public static int getDarkAccentColor(@Nullable final WallpaperColors wallpaperColors) {
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
