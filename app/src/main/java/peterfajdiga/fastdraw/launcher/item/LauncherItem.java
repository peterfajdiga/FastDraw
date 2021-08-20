package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;

import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.launcher.itemdisplay.DisplayItem;

public abstract class LauncherItem {
    public abstract String getID();
    @Nullable public abstract String getPackageName();
    public abstract DisplayItem getDisplayItem(Context context);
}
