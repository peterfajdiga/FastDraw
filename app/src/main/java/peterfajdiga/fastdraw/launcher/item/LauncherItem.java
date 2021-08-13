package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.launcher.LaunchManager;

public abstract class LauncherItem implements Comparable<LauncherItem> {
    public abstract String getID();
    public abstract CharSequence getLabel();
    public abstract Drawable getIcon();
    public abstract void launch(Context context, LaunchManager launchManager, Bundle opts, Rect clipBounds);
    @Nullable public abstract String getPackageName();

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.getLabel().toString().compareToIgnoreCase(other.getLabel().toString());
    }
}
