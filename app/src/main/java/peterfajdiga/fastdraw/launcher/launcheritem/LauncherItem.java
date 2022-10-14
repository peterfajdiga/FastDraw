package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;

public interface LauncherItem {
    @NonNull String getId();
    @Nullable String getPackageName();
    @NonNull DisplayItem getDisplayItem(Context context);
}
