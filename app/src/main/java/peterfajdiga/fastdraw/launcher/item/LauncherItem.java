package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;

import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.launcher.itemdisplay.DisplayItem;

public interface LauncherItem {
    String getID();
    @Nullable String getPackageName();
    DisplayItem getDisplayItem(Context context);
}
