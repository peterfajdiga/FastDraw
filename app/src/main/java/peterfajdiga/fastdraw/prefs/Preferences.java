package peterfajdiga.fastdraw.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import peterfajdiga.fastdraw.R;

public final class Preferences {
    public static boolean allowOrientation;
    public static boolean appsLinearList;
    public static int widgetHeight;
    public static boolean headerOnBottom;
    public static boolean scrollableTabs;
    public static boolean hideHidden;

    public static boolean wallpaperParallax;
    public static boolean headerSeparator;
    public static boolean bgGradientColorFromWallpaper;
    public static int bgGradientOpacity;

    public static void loadPreferences(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final Resources res = context.getResources();

        switch (prefs.getString("appItemResource", res.getString(R.string.default_appItemResource))) {
            default:
            case "0": appsLinearList = false; break;
            case "1": appsLinearList = true; break;
        }
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
