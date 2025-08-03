package peterfajdiga.fastdraw.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.R;

public final class Preferences {
    public final boolean allowOrientation;
    public final boolean switchToHome;
    public final boolean headerOnBottom;
    public final boolean scrollableTabs;
    public final boolean hideHidden;
    public final boolean wallpaperParallax;
    public final boolean headerDivider;
    public final boolean bgGradientColorFromWallpaper;
    public final int bgGradientOpacity;

    private final SharedPreferences prefs;
    private final Resources res;

    public Preferences(@NonNull final Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.res = context.getResources();

        allowOrientation             = this.prefs.getBoolean("allowOrientation",             this.res.getBoolean(R.bool.default_allowOrientation));
        switchToHome                 = this.prefs.getBoolean("switchToHome",                 this.res.getBoolean(R.bool.default_switchToHome));
        headerOnBottom               = this.prefs.getBoolean("headerbtm",                    this.res.getBoolean(R.bool.default_headerbtm));
        scrollableTabs               = this.prefs.getBoolean("scrollableTabs",               this.res.getBoolean(R.bool.default_scrollableTabs));
        hideHidden                   = this.prefs.getBoolean("hideHidden",                   this.res.getBoolean(R.bool.default_hideHidden));
        wallpaperParallax            = this.prefs.getBoolean("wallpaperParallax",            this.res.getBoolean(R.bool.default_wallpaperParallax));
        headerDivider                = this.prefs.getBoolean("headerDivider",                this.res.getBoolean(R.bool.default_headerDivider));
        bgGradientColorFromWallpaper = this.prefs.getBoolean("bgGradientColorFromWallpaper", this.res.getBoolean(R.bool.default_bgGradientColorFromWallpaper));
        bgGradientOpacity            = this.prefs.getInt("bgGradientOpacity",                this.res.getInteger(R.integer.default_bgGradientOpacity));
    }

    public int getWidgetHeight() {
        return this.prefs.getInt("widgetHeight", this.res.getInteger(R.integer.default_widgetHeight));
    }

    public void setWidgetHeight(final int value) {
        this.prefs.edit().putInt("widgetHeight", value).apply();
    }
}
