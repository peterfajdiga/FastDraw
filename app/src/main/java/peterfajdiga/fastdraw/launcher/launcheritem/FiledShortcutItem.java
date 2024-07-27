package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.ShortcutItemManager;

public abstract class FiledShortcutItem implements ShortcutItem, Saveable {
    protected final ShortcutItemManager shortcutItemManager;
    public FiledShortcutItem(@NonNull final ShortcutItemManager shortcutItemManager) {
        this.shortcutItemManager = shortcutItemManager;
    }

    @NonNull abstract String getTypeKey();
    @NonNull abstract String getUUID();

    public String getFilename() {
        return getTypeKey() + "_" + getUUID();
    }

    @Override
    public void delete(final Context context) {
        shortcutItemManager.deleteShortcut(context, this);
    }
}

