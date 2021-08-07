package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.LaunchManager;

public abstract class LauncherItem implements Comparable<LauncherItem> {
    public abstract String getID();
    public abstract CharSequence getLabel();
    public abstract Drawable getIcon();
    public abstract void launch(Context context, LaunchManager launchManager, View view);
    public abstract boolean isRemovable();

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.getLabel().toString().compareToIgnoreCase(other.getLabel().toString());
    }
}
