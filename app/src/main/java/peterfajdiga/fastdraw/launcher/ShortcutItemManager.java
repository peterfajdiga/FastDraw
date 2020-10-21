package peterfajdiga.fastdraw.launcher;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class ShortcutItemManager {
    private ShortcutItemManager() {}

    @NonNull
    public static ShortcutItem[] getShortcutItems(@NonNull final Context context) {
        final List<ShortcutItem> shortcuts = new ArrayList<>();
        final File shortcutsDir = ShortcutItem.getShortcutsDir(context);
        shortcutsDir.mkdir();
        for (final File file : shortcutsDir.listFiles()) {
            try {
                final ShortcutItem item = ShortcutItem.fromFile(context, file);
                shortcuts.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return shortcuts.toArray(new ShortcutItem[0]);
    }

    public static void saveShortcut(@NonNull final Context context, @NonNull final ShortcutItem shortcutItem) {
        try {
            shortcutItem.toFile(context);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: handle
        }
    }

    public static void deleteShortcut(@NonNull final Context context, @NonNull final ShortcutItem shortcutItem) {
        final File file = new File(getShortcutsDir(context), getShortcutFilename(shortcutItem));
        file.delete();
    }

    @NonNull
    private static File getShortcutsDir(@NonNull final Context context) {
        return new File(context.getFilesDir(), "shortcuts");
    }

    @NonNull
    private static String getShortcutFilename(@NonNull final ShortcutItem shortcutItem) {
        return shortcutItem.getID().replace('\0', '_');
    }
}
