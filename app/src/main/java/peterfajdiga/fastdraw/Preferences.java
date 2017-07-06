package peterfajdiga.fastdraw;

import android.content.Context;
import android.content.SharedPreferences;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        switch (prefs.getString("appItemResource", "0")) {
            default:
            case "0": appItemResource = R.layout.app_item; break;
            case "1": appItemResource = R.layout.app_item_horizontal; break;
            case "2": appItemResource = R.layout.app_item_package; break;
        }
        mainLayoutResource = prefs.getBoolean("headerbtm", false) ? R.layout.activity_main_headerbtm : R.layout.activity_main_headertop;
        stackFromBottom = prefs.getBoolean("stackFromBottom", false);
        statusBarDarker = prefs.getBoolean("statusBarDarker", false);
        longclickAction = Integer.parseInt(prefs.getString("action_longclick", "1"));
        doubleclickAction = Integer.parseInt(prefs.getString("action_doubleclick", "2"));
        pinchAction = Integer.parseInt(prefs.getString("action_pinch", "0"));
        unpinchAction = Integer.parseInt(prefs.getString("action_unpinch", "0"));
        headerBgColor = prefs.getInt("headerBgColor", 0x80000000);
        headerBgColorExpanded = prefs.getInt("headerBgColorExpanded", 0xB0000000);
    }
}
