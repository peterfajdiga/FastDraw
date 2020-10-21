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
}
