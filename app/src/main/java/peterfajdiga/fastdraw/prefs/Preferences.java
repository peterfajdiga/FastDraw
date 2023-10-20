package peterfajdiga.fastdraw.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.R;

public final class Preferences {
    public final boolean allowOrientation;
    public final int widgetHeight;
    public final boolean headerOnBottom;
    public final boolean scrollableTabs;
    public final boolean hideHidden;
    public final boolean wallpaperParallax;
    public final boolean headerSeparator;
    public final boolean bgGradientColorFromWallpaper;
    public final int bgGradientOpacity;

    public Preferences(@NonNull final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final Resources res = context.getResources();

        allowOrientation             = prefs.getBoolean("allowOrientation",             res.getBoolean(R.bool.default_allowOrientation));
        widgetHeight                 = prefs.getInt("widgetHeight",                     res.getInteger(R.integer.default_widgetHeight));
        headerOnBottom               = prefs.getBoolean("headerbtm",                    res.getBoolean(R.bool.default_headerbtm));
        scrollableTabs               = prefs.getBoolean("scrollableTabs",               res.getBoolean(R.bool.default_scrollableTabs));
        hideHidden                   = prefs.getBoolean("hideHidden",                   res.getBoolean(R.bool.default_hideHidden));
        wallpaperParallax            = prefs.getBoolean("wallpaperParallax",            res.getBoolean(R.bool.default_wallpaperParallax));
        headerSeparator              = prefs.getBoolean("headerSeparator",              res.getBoolean(R.bool.default_headerSeparator));
        bgGradientColorFromWallpaper = prefs.getBoolean("bgGradientColorFromWallpaper", res.getBoolean(R.bool.default_bgGradientColorFromWallpaper));
        bgGradientOpacity            = prefs.getInt("bgGradientOpacity",                res.getInteger(R.integer.default_bgGradientOpacity));
    }
}
