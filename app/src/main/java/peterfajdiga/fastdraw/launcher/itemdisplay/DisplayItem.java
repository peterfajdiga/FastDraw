package peterfajdiga.fastdraw.launcher.itemdisplay;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.LaunchManager;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

public class DisplayItem implements Comparable<DisplayItem> {
    private final String id;
    private final CharSequence label;
    private final Drawable icon;
    private final Launchable launchable;
    private final LauncherItem source;

    public DisplayItem(
        final String id,
        final CharSequence label,
        final Drawable icon,
        final Launchable launchable,
        final LauncherItem source
    ) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.launchable = launchable;
        this.source = source;
    }

    public String getID() {
        return id;
    }

    public CharSequence getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void launch(final Context context, final LaunchManager launchManager, final Bundle opts, final Rect clipBounds) {
        launchable.launch(context, launchManager, opts, clipBounds);
    }

    public LauncherItem getLauncherItem() {
        return source;
    }

    @Override
    public int compareTo(@NonNull DisplayItem other) {
        return this.getLabel().toString().compareToIgnoreCase(other.getLabel().toString());
    }
}
