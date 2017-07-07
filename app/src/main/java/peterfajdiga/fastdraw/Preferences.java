package peterfajdiga.fastdraw;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public final class Preferences {

    public static final int ACTION_WALLPAPER = 1;
    public static final int ACTION_SHORTCUT  = 2;
    public static final int ACTION_SETTINGS  = 3;

    private static int appItemResource;
    public static int appItemResource() {
        return appItemResource;
    }

    private static int mainLayoutResource;
    public static int mainLayoutResource() {
        return mainLayoutResource;
    }

    private static boolean stackFromBottom;
    public static boolean stackFromBottom() {
        return stackFromBottom;
    }

    private static boolean showIcons;
    public static boolean showIcons() {
        return showIcons;
    }

    private static int headerBgColor;
    public static int headerBgColor() {
        return headerBgColor;
    }

    private static int headerBgColorExpanded;
    public static int headerBgColorExpanded() {
        return headerBgColorExpanded;
    }

    private static boolean statusBarDarker;
    public static boolean statusBarDarker() {
        return statusBarDarker;
    }

    private static int longclickAction;
    public static int longclickAction() {
        return longclickAction;
    }

    private static int doubleclickAction;
    public static int doubleclickAction() {
        return doubleclickAction;
    }

    private static int pinchAction;
    public static int pinchAction() {
        return pinchAction;
    }

    private static int unpinchAction;
    public static int unpinchAction() {
        return unpinchAction;
    }


    public static void loadPreferences(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final Resources res = context.getResources();

        switch (prefs.getString("appItemResource", res.getString(R.string.default_appItemResource))) {
            default:
            case "0": appItemResource = R.layout.app_item; break;
            case "1": appItemResource = R.layout.app_item_horizontal; break;
            case "2": appItemResource = R.layout.app_item_package; break;
        }
        mainLayoutResource                 = prefs.getBoolean("headerbtm",         res.getBoolean(R.bool.default_headerbtm)) ? R.layout.activity_main_headerbtm : R.layout.activity_main_headertop;
        stackFromBottom                    = prefs.getBoolean("stackFromBottom",   res.getBoolean(R.bool.default_stackFromBottom));
        showIcons                          = prefs.getBoolean("showIcons",         res.getBoolean(R.bool.default_showIcons));
        statusBarDarker                    = prefs.getBoolean("statusBarDarker",   res.getBoolean(R.bool.default_statusBarDarker));
        longclickAction   = Integer.parseInt(prefs.getString("action_longclick",   res.getString(R.string.default_action_longclick)));
        doubleclickAction = Integer.parseInt(prefs.getString("action_doubleclick", res.getString(R.string.default_action_doubleclick)));
        pinchAction       = Integer.parseInt(prefs.getString("action_pinch",       res.getString(R.string.default_action_pinch)));
        unpinchAction     = Integer.parseInt(prefs.getString("action_unpinch",     res.getString(R.string.default_action_unpinch)));
        headerBgColor                      = prefs.getInt("headerBgColor",         res.getInteger(R.integer.default_headerBgColor));
        headerBgColorExpanded              = prefs.getInt("headerBgColorExpanded", res.getInteger(R.integer.default_headerBgColorExpanded));
    }
}
