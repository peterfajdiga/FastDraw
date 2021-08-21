package peterfajdiga.fastdraw.launcher.displayitem;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.launchable.Launchable;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;

public class DisplayItem implements Comparable<DisplayItem> {
    public final String id;
    public final CharSequence label;
    public final Drawable icon;
    public final LauncherItem source;
    public final Launchable launchable;

    public DisplayItem(
        final String id,
        final CharSequence label,
        final Drawable icon,
        final LauncherItem source,
        final Launchable launchable
    ) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.source = source;
        this.launchable = launchable;
    }

    @Override
    public int compareTo(@NonNull DisplayItem other) {
        return this.label.toString().compareToIgnoreCase(other.label.toString());
    }
}
