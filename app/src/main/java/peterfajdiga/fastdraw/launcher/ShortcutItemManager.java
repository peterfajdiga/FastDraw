package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import peterfajdiga.fastdraw.launcher.item.BitmapShortcutItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.OreoShortcutItem;
import peterfajdiga.fastdraw.launcher.item.OreoShortcuts;
import peterfajdiga.fastdraw.launcher.item.ResShortcutItem;
import peterfajdiga.fastdraw.launcher.item.Saveable;

public class ShortcutItemManager {
    private ShortcutItemManager() {}

    @NonNull
    public static List<LauncherItem> getShortcutItems(@NonNull final Context context) {
        final List<LauncherItem> shortcuts = new ArrayList<>();
        final File shortcutsDir = getShortcutsDir(context);
        shortcutsDir.mkdir();
        for (final File file : shortcutsDir.listFiles()) {
            try {
                final LauncherItem item = readLauncherItem(context, file);
                if (item == null) {
                    continue;
                }
                shortcuts.add(item);
            } catch (final IOException | URISyntaxException e) {
                Log.e("ShortcutItemManager", "Failed to read shortcut " + file.getName(), e);
            }
        }
        return shortcuts;
    }

    @Nullable
    private static LauncherItem readLauncherItem(@NonNull final Context context, @NonNull final File file) throws IOException, URISyntaxException {
        final String typeKey = getTypeKey(file);
        switch (typeKey) {
            case BitmapShortcutItem.TYPE_KEY:
                return BitmapShortcutItem.fromFile(context, file);
            case ResShortcutItem.TYPE_KEY:
                return ResShortcutItem.fromFile(context, file);
            case OreoShortcutItem.TYPE_KEY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        return OreoShortcutItem.fromFile(context, file);
                    } catch (final Saveable.LeftoverException e) {
                        file.delete();
                        return null;
                    }
                } else {
                    return null;
                }
            default:
                Log.w("ShortcutItemManager", "Unknown file type key: " + typeKey);
                return null;
        }
    }

    @NonNull
    private static String getTypeKey(@NonNull final File file) {
        final String filename = file.getName();
        final int index = filename.indexOf("_");
        return filename.substring(0, index);
    }

    public static void saveShortcut(@NonNull final Context context, @NonNull final Saveable item) {
        try {
            final File file = new File(getShortcutsDir(context), item.getFilename());
            item.toFile(file);
        } catch (final IOException e) {
            Log.e("ShortcutItemManager", "Failed to save shortcut " + item.getFilename(), e); // TODO: handle? how?
        }
    }

    public static void deleteShortcut(@NonNull final Context context, @NonNull final Saveable item) {
        final File file = new File(getShortcutsDir(context), item.getFilename());
        file.delete();
    }

    @NonNull
    private static File getShortcutsDir(@NonNull final Context context) {
        return new File(context.getFilesDir(), "shortcuts");
    }

    @NonNull
    public static LauncherItem shortcutFromIntent(@NonNull final Context context, @NonNull final Intent data) {
        final Intent launchIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        final String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        final Bitmap bmp = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (bmp != null) {
            final BitmapDrawable icon = new BitmapDrawable(context.getResources(), bmp);
            return new BitmapShortcutItem(launchIntent, generateSalt(), name, icon);
        } else {
            final Intent.ShortcutIconResource iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            return new ResShortcutItem(launchIntent, generateSalt(), name, iconResource.packageName, iconResource.resourceName);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public static OreoShortcutItem oreoShortcutFromIntent(@NonNull final Context context, @NonNull final Intent data) {
        final LauncherApps.PinItemRequest pinItemRequest = data.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST);
        if (!pinItemRequest.accept()) {
            return null;
        }
        final ShortcutInfo shortcutInfo = pinItemRequest.getShortcutInfo();

        return new OreoShortcutItem(
            shortcutInfo.getPackage(),
            shortcutInfo.getId(),
            OreoShortcuts.getLabel(shortcutInfo),
            OreoShortcuts.getIcon(context, shortcutInfo)
        );
    }

    @NonNull
    private static String generateSalt() {
        return UUID.randomUUID().toString();
    }
}
