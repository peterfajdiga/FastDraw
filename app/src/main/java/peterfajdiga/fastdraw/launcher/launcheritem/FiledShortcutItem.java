package peterfajdiga.fastdraw.launcher.launcheritem;

import androidx.annotation.NonNull;

public interface FiledShortcutItem extends ShortcutItem, Saveable {
    @NonNull String getTypeKey();
    @NonNull String getUUID();

    static String getFilename(@NonNull FiledShortcutItem item) {
        return item.getTypeKey() + "_" + item.getUUID();
    }
}
