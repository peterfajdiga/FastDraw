package peterfajdiga.fastdraw;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public final class Preferences {
    public static final int ACTION_MENU = 1;
    public static final int ACTION_WALLPAPER = 2;
    public static final int ACTION_SHORTCUT = 3;
    public static final int ACTION_RENAME_CATEGORY = 4;
    public static final int ACTION_SETTINGS = 5;
    public static final int ACTION_NOTIFICATIONS = 6;

    public static boolean allowOrientation;

    public static boolean appsLinearList;

    public static boolean headerOnBottom;
    public static boolean stackFromBottom;

    public static boolean scrollableTabs;

    public static boolean hideHidden;

    public static boolean showIcons;
    public static boolean largeSingle;

    public static boolean headerSeparator;
    public static boolean headerShadow;

    public static int headerBgColor;
    public static int headerBgColorExpanded;

    public static int widgetHeight;

    public static int longclickAction;
    public static int doubleclickAction;
    public static int pinchAction;
    public static int unpinchAction;
    public static int swipeUpAction2F;
    public static int swipeDownAction2F;

    public static void loadPreferences(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final Resources res = context.getResources();

        switch (prefs.getString("appItemResource", res.getString(R.string.default_appItemResource))) {
            default:
            case "0": appsLinearList = false; break;
            case "1": appsLinearList = true; break;
        }
        allowOrientation                   = prefs.getBoolean("allowOrientation",    res.getBoolean(R.bool.default_allowOrientation));
        headerOnBottom                     = prefs.getBoolean("headerbtm",           res.getBoolean(R.bool.default_headerbtm));
        stackFromBottom                    = prefs.getBoolean("stackFromBottom",     res.getBoolean(R.bool.default_stackFromBottom));
        scrollableTabs                     = prefs.getBoolean("scrollableTabs",      res.getBoolean(R.bool.default_scrollableTabs));
        hideHidden                         = prefs.getBoolean("hideHidden",          res.getBoolean(R.bool.default_hideHidden));
        showIcons                          = prefs.getBoolean("showIcons",           res.getBoolean(R.bool.default_showIcons));
        largeSingle                        = prefs.getBoolean("largeSingle",         res.getBoolean(R.bool.default_largeSingle));
        headerSeparator                    = prefs.getBoolean("headerSeparator",     res.getBoolean(R.bool.default_headerSeparator));
        headerShadow                       = prefs.getBoolean("headerShadow",        res.getBoolean(R.bool.default_headerShadow));
        longclickAction   = Integer.parseInt(prefs.getString("action_longclick",     res.getString(R.string.default_action_longclick)));
        doubleclickAction = Integer.parseInt(prefs.getString("action_doubleclick",   res.getString(R.string.default_action_doubleclick)));
        pinchAction       = Integer.parseInt(prefs.getString("action_pinch",         res.getString(R.string.default_action_pinch)));
        unpinchAction     = Integer.parseInt(prefs.getString("action_unpinch",       res.getString(R.string.default_action_unpinch)));
        swipeUpAction2F   = Integer.parseInt(prefs.getString("action_swipe_up_2f",   res.getString(R.string.default_action_swipe_up_2f)));
        swipeDownAction2F = Integer.parseInt(prefs.getString("action_swipe_down_2f", res.getString(R.string.default_action_swipe_down_2f)));
        headerBgColor                      = prefs.getInt("headerBgColor",           res.getInteger(R.integer.default_headerBgColor));
        headerBgColorExpanded              = prefs.getInt("headerBgColorExpanded",   res.getInteger(R.integer.default_headerBgColorExpanded));
        widgetHeight                       = prefs.getInt("widgetHeight",            res.getInteger(R.integer.default_widgetHeight));
    }
}
