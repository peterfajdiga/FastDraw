package peterfajdiga.fastdraw.launcher;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class ShortcutItemManager {
    private ShortcutItemManager() {}

    public static void addShortcutItems(@NonNull final Context context, @NonNull final LauncherPager pager) {
        final List<LauncherItem> shortcuts = new ArrayList<>();
        final File shortcutsDir = ShortcutItem.getShortcutsDir(context);
        shortcutsDir.mkdir();
        for (File file : shortcutsDir.listFiles()) {
            try {
                final ShortcutItem item = ShortcutItem.fromFile(context, file);
                shortcuts.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pager.addLauncherItems("LOST&FOUND", shortcuts.toArray(new LauncherItem[0]));
    }
}
