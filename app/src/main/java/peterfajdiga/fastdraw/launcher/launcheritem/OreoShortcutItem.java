package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.OreoShortcuts;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;
import peterfajdiga.fastdraw.launcher.launchable.OreoShortcutLaunchable;

public class OreoShortcutItem implements ShortcutItem {
    public static final String TYPE_KEY = "oreo";

    private final ShortcutInfo info;
    private DisplayItem displayItem = null;

    public OreoShortcutItem(@NonNull final ShortcutInfo info) {
        this.info = info;
    }

    @NonNull
    @Override
    public String getId() {
        return String.join(
            "\0",
            TYPE_KEY,
            info.getPackage(),
            info.getId()
        );
    }

    @Override
    @NonNull
    public String getPackageName() {
        return info.getPackage();
    }

    @NonNull
    @Override
    public DisplayItem getDisplayItem(final Context context) {
        if (displayItem != null) {
            return displayItem;
        }

        final CharSequence label = OreoShortcuts.getLabel(info);
        final Drawable icon = OreoShortcuts.getIcon(context, info);
        final Launchable launchable = new OreoShortcutLaunchable(info);
        this.displayItem = new DisplayItem(getId(), label, icon, this, launchable);
        return displayItem;
    }

    @Override
    public void delete(final Context context) {
        OreoShortcuts.unpinShortcut(context, info.getPackage(), info.getId());
    }
}
