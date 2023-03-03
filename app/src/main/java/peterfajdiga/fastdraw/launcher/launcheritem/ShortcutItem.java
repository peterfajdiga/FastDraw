package peterfajdiga.fastdraw.launcher.launcheritem;

import androidx.annotation.NonNull;

public interface ShortcutItem extends LauncherItem, Saveable {
    @NonNull String getTypeKey();
    @NonNull String getUUID();

    static String getFilename(@NonNull ShortcutItem item) {
        return item.getTypeKey() + "_" + item.getUUID();
    }
}
