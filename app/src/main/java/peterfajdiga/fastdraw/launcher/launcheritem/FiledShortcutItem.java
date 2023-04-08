package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.ShortcutItemManager;

public abstract class FiledShortcutItem implements ShortcutItem, Saveable {
    @NonNull abstract String getTypeKey();
    @NonNull abstract String getUUID();

    public static String getFilename(@NonNull FiledShortcutItem item) {
        return item.getTypeKey() + "_" + item.getUUID();
    }

    @Override
    public void delete(final Context context) {
        ShortcutItemManager.deleteShortcut(context, this);
    }
}

