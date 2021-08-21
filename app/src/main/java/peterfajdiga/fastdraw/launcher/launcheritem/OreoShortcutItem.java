package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import peterfajdiga.fastdraw.launcher.OreoShortcuts;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;
import peterfajdiga.fastdraw.launcher.launchable.OreoShortcutLaunchable;

public class OreoShortcutItem implements ShortcutItem {
    public static final String TYPE_KEY = "oreo";

    private final String packageName;
    private final String oreoShortcutId;
    private final String uuid;
    private final DisplayItem displayItem;

    // TODO: load label and icon in getDisplayItem
    @RequiresApi(api = Build.VERSION_CODES.O)
    public OreoShortcutItem(
        @NonNull final String packageName,
        @NonNull final String oreoShortcutId,
        @NonNull final CharSequence label,
        @NonNull final Drawable icon,
        @NonNull final String uuid
    ) {
        this.packageName = packageName;
        this.oreoShortcutId = oreoShortcutId;
        this.uuid = uuid;
        final Launchable launchable = new OreoShortcutLaunchable(packageName, oreoShortcutId);
        this.displayItem = new DisplayItem(getID(), label, icon, launchable, this);
    }

    @Override
    public String getID() {
        return TYPE_KEY + "\0" + uuid;
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
    public String getFilename() {
        return getID().replace('\0', '_');
    }

    @Override
    public void save(@NonNull final OutputStream out) throws IOException {
        Saveable.writeString(out, packageName);
        Saveable.writeString(out, oreoShortcutId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public static OreoShortcutItem fromFile(
        @NonNull final Context context,
        @NonNull final InputStream in,
        @NonNull final String uuid
    ) throws java.io.IOException, LeftoverException {
        final String packageName = Saveable.readString(in);
        final String oreoShortcutId = Saveable.readString(in);

        final ShortcutInfo shortcutInfo = OreoShortcuts.getShortcutInfo(context, packageName, oreoShortcutId);
        if (shortcutInfo == null) {
            return null;
        }

        return new OreoShortcutItem(
            packageName,
            oreoShortcutId,
            OreoShortcuts.getLabel(shortcutInfo),
            OreoShortcuts.getIcon(context, shortcutInfo),
            uuid
        );
    }
}
