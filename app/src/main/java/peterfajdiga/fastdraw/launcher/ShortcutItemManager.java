package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import peterfajdiga.fastdraw.launcher.launcheritem.BitmapShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.FiledShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;
import peterfajdiga.fastdraw.launcher.launcheritem.OreoShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.ResShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.Saveable;

public class ShortcutItemManager {
    private final StatisticsManager statisticsManager;

    public ShortcutItemManager(@NonNull final StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
    }

    @NonNull
    public Stream<LauncherItem> getShortcutItems(@NonNull final Context context) {
        final Stream<LauncherItem> filedShortcuts = getFiledShortcutItems(context);
        final Stream<LauncherItem> oreoShortcuts = getOreoShortcutItems(context);
        return Stream.concat(filedShortcuts, oreoShortcuts);
    }

    private Stream<LauncherItem> getFiledShortcutItems(@NonNull final Context context) {
        final File shortcutsDir = getShortcutsDir(context);
        shortcutsDir.mkdir();
        return Arrays.stream(shortcutsDir.listFiles()).map(file -> {
            try {
                return (LauncherItem)readShortcutItem(context, file);
            } catch (final IOException | URISyntaxException e) {
                Log.e("ShortcutItemManager", "Failed to read shortcut " + file.getName(), e);
            } catch (final Saveable.LeftoverException e) {
                Log.i("ShortcutItemManager", "Leftover file " + file.getName());
                file.delete();
            }
            return null;
        }).filter(Objects::nonNull);
    }

    @NonNull
    private Stream<LauncherItem> getOreoShortcutItems(@NonNull final Context context) {
        final List<ShortcutInfo> shortcuts = OreoShortcuts.getPinnedShortcuts(context);
        if (shortcuts == null) {
            return Stream.empty();
        }

        return shortcuts.stream().map(OreoShortcutItem::new);
    }

    @Nullable
    private FiledShortcutItem readShortcutItem(
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

        try (final FileInputStream in = new FileInputStream(file)) {
            return readShortcutItem(context, in, typeKey, uuid);
        }
    }

    @Nullable private FiledShortcutItem readShortcutItem(
        @NonNull final Context context,
        @NonNull final FileInputStream in,
        @NonNull final String typeKey,
        @NonNull final String uuid
    ) throws IOException, URISyntaxException, Saveable.LeftoverException {
        switch (typeKey) {
            case BitmapShortcutItem.TYPE_KEY:
                return BitmapShortcutItem.fromFile(this, statisticsManager, context, in, uuid);
            case ResShortcutItem.TYPE_KEY:
                return ResShortcutItem.fromFile(this, statisticsManager, context, in, uuid);
            case OreoShortcutItem.TYPE_KEY:
                throw new Saveable.LeftoverException();
            default:
                Log.w("ShortcutItemManager", "Unknown file type key: " + typeKey);
                return null;
        }
    }

    public void saveShortcut(@NonNull final Context context, @NonNull final FiledShortcutItem item) {
        final String filename = item.getFilename();
        try {
            final File file = new File(getShortcutsDir(context), filename);
            try (final FileOutputStream out = new FileOutputStream(file)) {
                item.save(out);
            }
        } catch (final IOException e) {
            Log.e("ShortcutItemManager", "Failed to save shortcut " + filename, e);
        }
    }

    public void deleteShortcut(@NonNull final Context context, @NonNull final FiledShortcutItem item) {
        final String filename = item.getFilename();
        final File file = new File(getShortcutsDir(context), filename);
        file.delete();
    }

    @NonNull
    private File getShortcutsDir(@NonNull final Context context) {
        return new File(context.getFilesDir(), "shortcuts");
    }

    @NonNull
    public FiledShortcutItem shortcutFromIntent(@NonNull final Context context, @NonNull final Intent intent) {
        final Intent launchIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        final String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        final Bitmap bmp = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (bmp != null) {
            final BitmapDrawable icon = new BitmapDrawable(context.getResources(), bmp);
            return new BitmapShortcutItem(this, statisticsManager, generateUUID(), launchIntent, name, icon);
        } else {
            final Intent.ShortcutIconResource iconResource = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            return new ResShortcutItem(this, statisticsManager, generateUUID(), launchIntent, name, iconResource.packageName, iconResource.resourceName);
        }
    }

    @Nullable
    public OreoShortcutItem oreoShortcutFromIntent(final Context context, @NonNull final Intent intent) {
        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        final LauncherApps.PinItemRequest pinItemRequest = launcherApps.getPinItemRequest(intent);
        if (!pinItemRequest.isValid()) {
            return null;
        }
        if (!pinItemRequest.accept()) {
            return null;
        }
        return new OreoShortcutItem(pinItemRequest.getShortcutInfo());
    }

    @NonNull
    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
