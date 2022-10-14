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

    private final String uuid;
    private final ShortcutInfo info;
    private DisplayItem displayItem = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public OreoShortcutItem(@NonNull final String uuid, @NonNull final ShortcutInfo info) {
        this.uuid = uuid;
        this.info = info;
    }

    @NonNull
    @Override
    public String getId() {
        return TYPE_KEY + "\0" + uuid;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    @Override
    @NonNull
    public String getPackageName() {
        return info.getPackage();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    @Override
    public void save(@NonNull final OutputStream out) throws IOException {
        Saveable.writeString(out, info.getPackage());
        Saveable.writeString(out, info.getId());
    }

    @NonNull
    @Override
    public String getTypeKey() {
        return TYPE_KEY;
    }

    @NonNull
    @Override
    public String getUUID() {
        return uuid;
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

        return new OreoShortcutItem(uuid, shortcutInfo);
    }
}
