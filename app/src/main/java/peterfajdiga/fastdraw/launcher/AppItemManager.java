package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import peterfajdiga.fastdraw.launcher.launcheritem.AppItem;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;

public class AppItemManager {
    private AppItemManager() {}

    @NonNull
    private static Stream<AppItem> getAppItems(@NonNull final PackageManager packageManager, @NonNull final Intent launcherIntent) {
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> appActivities = packageManager.queryIntentActivities(launcherIntent, 0);
        return appActivities.stream().map(resolveInfo -> new AppItem(resolveInfo.activityInfo));
    }

    @NonNull
    public static Stream<AppItem> getAppItems(@NonNull final PackageManager packageManager) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        return getAppItems(packageManager, launcherIntent);
    }

    @NonNull
    public static Stream<AppItem> getAppItems(@NonNull final PackageManager packageManager, @NonNull final String packageName) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        launcherIntent.setPackage(packageName);
        return getAppItems(packageManager, launcherIntent);
    }

    public static void removePackageItems(final Launcher launcher, final String packageName) {
        for (final LauncherItem item : launcher.getItems()) {
            if (packageName.equals(item.getPackageName()) && item instanceof AppItem) {
                launcher.removeItem(item, false);
            }
        }
    }

    public static void updatePackageItems(
        final Launcher launcher,
        final String packageName,
        final Stream<AppItem> updatedAppItemsStream
    ) {
        final Map<String, AppItem> updatedAppItems = updatedAppItemsStream.collect(Collectors.toMap(AppItem::getId, Function.identity()));

        for (final LauncherItem existingItem : launcher.getItems()) {
            if (existingItem instanceof AppItem && packageName.equals(existingItem.getPackageName())) {
                final AppItem updatedAppItem = updatedAppItems.remove(existingItem.getId());
                if (updatedAppItem == null) {
                    launcher.removeItem(existingItem, true);
                } else {
                    launcher.updateItem(existingItem, updatedAppItem);
                }
            }
        }

        launcher.addItems(updatedAppItems.values().toArray(new AppItem[0]));
    }

    public static void showPackageDetails(final Context context, final String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }
}
