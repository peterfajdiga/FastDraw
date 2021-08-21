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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import peterfajdiga.fastdraw.launcher.item.BitmapShortcutItem;
import peterfajdiga.fastdraw.launcher.item.OreoShortcutItem;
import peterfajdiga.fastdraw.launcher.item.ResShortcutItem;
import peterfajdiga.fastdraw.launcher.item.Saveable;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class ShortcutItemManager {
    private ShortcutItemManager() {}

    @NonNull
    public static List<ShortcutItem> getShortcutItems(@NonNull final Context context) {
        final List<ShortcutItem> shortcuts = new ArrayList<>();
        final File shortcutsDir = getShortcutsDir(context);
        shortcutsDir.mkdir();
        for (final File file : shortcutsDir.listFiles()) {
            try {
                final ShortcutItem item = readShortcutItem(context, file);
                if (item == null) {
                    continue;
                }
                shortcuts.add(item);
            } catch (final IOException | URISyntaxException e) {
                Log.e("ShortcutItemManager", "Failed to read shortcut " + file.getName(), e);
            } catch (final Saveable.LeftoverException e) {
                Log.i("ShortcutItemManager", "Leftover file " + file.getName());
                file.delete();
            }
        }
        return shortcuts;
    }

    @Nullable
    private static ShortcutItem readShortcutItem(
        @NonNull final Context context,
        @NonNull final File file
    ) throws IOException, URISyntaxException, Saveable.LeftoverException {
        final String[] filenameParts = file.getName().split("_", 2);
        if (filenameParts.length != 2) {
            Log.w("ShortcutItemManager", "Invalid filename: " + file.getName());
            return null;
        }
        final String typeKey = filenameParts[0];
        final String uuid = filenameParts[1];

        final FileInputStream in = new FileInputStream(file);
        final ShortcutItem item = readShortcutItem(context, in, typeKey, uuid);
        in.close();
        return item;
    }

    @Nullable private static ShortcutItem readShortcutItem(
        @NonNull final Context context,
        @NonNull final FileInputStream in,
        @NonNull final String typeKey,
        @NonNull final String uuid
    ) throws IOException, URISyntaxException, Saveable.LeftoverException {
        switch (typeKey) {
            case BitmapShortcutItem.TYPE_KEY:
                return BitmapShortcutItem.fromFile(context, in, uuid);
            case ResShortcutItem.TYPE_KEY:
                return ResShortcutItem.fromFile(context, in, uuid);
            case OreoShortcutItem.TYPE_KEY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return OreoShortcutItem.fromFile(context, in, uuid);
                } else {
                    return null;
                }
            default:
                Log.w("ShortcutItemManager", "Unknown file type key: " + typeKey);
                return null;
        }
    }

    public static void saveShortcut(@NonNull final Context context, @NonNull final Saveable item) {
        try {
            final File file = new File(getShortcutsDir(context), item.getFilename());
            final FileOutputStream out = new FileOutputStream(file);
            item.toFile(out);
            out.close();
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
    public static ShortcutItem shortcutFromIntent(@NonNull final Context context, @NonNull final Intent data) {
        final Intent launchIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        final String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        final Bitmap bmp = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (bmp != null) {
            final BitmapDrawable icon = new BitmapDrawable(context.getResources(), bmp);
            return new BitmapShortcutItem(name, icon, launchIntent, generateUUID());
        } else {
            final Intent.ShortcutIconResource iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            return new ResShortcutItem(name, iconResource.packageName, iconResource.resourceName, launchIntent, generateUUID());
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
            OreoShortcuts.getIcon(context, shortcutInfo),
            generateUUID()
        );
    }

    @NonNull
    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
