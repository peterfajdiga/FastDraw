package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;

import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;

public interface LauncherItem {
    String getID();
    @Nullable String getPackageName();
    DisplayItem getDisplayItem(Context context);
}
