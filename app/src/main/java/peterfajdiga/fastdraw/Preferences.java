package peterfajdiga.fastdraw;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public final class Preferences {
    public static boolean allowOrientation;
    public static boolean appsLinearList;
    public static int widgetHeight;
    public static boolean headerOnBottom;
    public static boolean scrollableTabs;
    public static boolean hideHidden;

    public static boolean wallpaperParallax;
    public static boolean showIcons;
    public static boolean largeSingle;
    public static boolean headerSeparator;
    public static boolean scrimColorFromWallpaper;
    public static int scrimOpacity;

    public static void loadPreferences(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final Resources res = context.getResources();

        switch (prefs.getString("appItemResource", res.getString(R.string.default_appItemResource))) {
            default:
            case "0": appsLinearList = false; break;
            case "1": appsLinearList = true; break;
        }
        allowOrientation                   = prefs.getBoolean("allowOrientation",        res.getBoolean(R.bool.default_allowOrientation));
        widgetHeight                       = prefs.getInt("widgetHeight",                res.getInteger(R.integer.default_widgetHeight));
        headerOnBottom                     = prefs.getBoolean("headerbtm",               res.getBoolean(R.bool.default_headerbtm));
        scrollableTabs                     = prefs.getBoolean("scrollableTabs",          res.getBoolean(R.bool.default_scrollableTabs));
        hideHidden                         = prefs.getBoolean("hideHidden",              res.getBoolean(R.bool.default_hideHidden));
        wallpaperParallax                  = prefs.getBoolean("wallpaperParallax",       res.getBoolean(R.bool.default_wallpaperParallax));
        showIcons                          = prefs.getBoolean("showIcons",               res.getBoolean(R.bool.default_showIcons));
        largeSingle                        = prefs.getBoolean("largeSingle",             res.getBoolean(R.bool.default_largeSingle));
        headerSeparator                    = prefs.getBoolean("headerSeparator",         res.getBoolean(R.bool.default_headerSeparator));
        scrimColorFromWallpaper            = prefs.getBoolean("scrimColorFromWallpaper", res.getBoolean(R.bool.default_scrimColorFromWallpaper));
        scrimOpacity                       = prefs.getInt("scrimOpacity",                res.getInteger(R.integer.default_scrimOpacity));
    }
}
