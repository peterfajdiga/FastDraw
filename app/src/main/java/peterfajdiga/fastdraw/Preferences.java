package peterfajdiga.fastdraw;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class Preferences {

    public static int appItemResource() {
        return R.layout.app_item;
    }

    private static int mainLayoutResource;
    public static int mainLayoutResource() {
        return mainLayoutResource;
    }

    private static boolean stackFromBottom;
    public static boolean stackFromBottom() {
        return stackFromBottom;
    }

    public static int headerBgColor() {
        return 0x80000000;
    }

    public static int headerBgColorExpanded() {
        return 0xB0000000;
    }

    private static boolean statusBarDarker;
    public static boolean statusBarDarker() {
        return statusBarDarker;
    }


    public static void loadPreferences(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mainLayoutResource = prefs.getBoolean("headerbtm", false) ? R.layout.activity_main_headerbtm : R.layout.activity_main_headertop;
        stackFromBottom = prefs.getBoolean("stackFromBottom", false);
        statusBarDarker = prefs.getBoolean("statusBarDarker", false);
    }
}
