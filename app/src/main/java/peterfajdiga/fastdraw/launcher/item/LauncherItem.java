package peterfajdiga.fastdraw.launcher.item;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.LaunchManager;

public abstract class LauncherItem implements Comparable<LauncherItem> {
    public abstract String getID();
    public abstract String getLabel();
    public abstract Drawable getIcon();
    public abstract void launch(LaunchManager launchManager, View view);

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.getLabel().compareToIgnoreCase(other.getLabel());
    }
}
