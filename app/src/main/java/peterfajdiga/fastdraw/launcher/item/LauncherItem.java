package peterfajdiga.fastdraw.launcher.item;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public abstract class LauncherItem implements Comparable<LauncherItem> {

    public Drawable icon = null;

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.getLabel().compareToIgnoreCase(other.getLabel());
    }

    public abstract String getID();
    public abstract String getLabel();
    public abstract Intent getIntent();
}
