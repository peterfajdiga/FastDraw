package peterfajdiga.fastdraw.launcher.item;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public abstract class LauncherItem implements Comparable<LauncherItem> {
    public abstract String getID();
    public abstract String getLabel();
    public abstract Drawable getIcon();
    public abstract Intent getIntent();

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.getLabel().compareToIgnoreCase(other.getLabel());
    }
}
