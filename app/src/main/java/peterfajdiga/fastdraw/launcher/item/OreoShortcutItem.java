package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import peterfajdiga.fastdraw.launcher.itemdisplay.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;
import peterfajdiga.fastdraw.launcher.launchable.OreoShortcutLaunchable;

@RequiresApi(api = Build.VERSION_CODES.O)
public class OreoShortcutItem implements LauncherItem, Saveable {
    public static final String TYPE_KEY = "oreo";

    private final String packageName;
    private final String oreoShortcutId;
    private final DisplayItem displayItem;

    // TODO: load label and icon in getDisplayItem
    public OreoShortcutItem(@NonNull final String packageName, @NonNull final String oreoShortcutId, @NonNull final CharSequence label, @NonNull final Drawable icon) {
        this.packageName = packageName;
        this.oreoShortcutId = oreoShortcutId;
        final Launchable launchable = new OreoShortcutLaunchable(packageName, oreoShortcutId);
        this.displayItem = new DisplayItem(getID(), label, icon, launchable, this);
    }

    @Override
    public String getID() {
        return TYPE_KEY + "\0" + (packageName + oreoShortcutId).hashCode(); // TODO: add salt?
    }

    @Override
    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @Override
    public DisplayItem getDisplayItem(final Context context) {
        return displayItem;
    }

    @Override
    public void toFile(@NonNull final File file) throws IOException {
        final FileOutputStream fos = new FileOutputStream(file);
        Saveable.writeString(fos, packageName);
        Saveable.writeString(fos, oreoShortcutId);
        fos.close();
    }

    @Override
    public String getFilename() {
        return getID().replace('\0', '_');
    }

    @Nullable
    public static OreoShortcutItem fromFile(@NonNull final Context context, @NonNull final File file) throws java.io.IOException, LeftoverException {
        final FileInputStream fis = new FileInputStream(file);
        final String packageName = Saveable.readString(fis);
        final String oreoShortcutId = Saveable.readString(fis);
        fis.close();

        final ShortcutInfo shortcutInfo = OreoShortcuts.getShortcutInfo(context, packageName, oreoShortcutId);
        if (shortcutInfo == null) {
            return null;
        }

        return new OreoShortcutItem(
            packageName,
            oreoShortcutId,
            OreoShortcuts.getLabel(shortcutInfo),
            OreoShortcuts.getIcon(context, shortcutInfo)
        );
    }
}
