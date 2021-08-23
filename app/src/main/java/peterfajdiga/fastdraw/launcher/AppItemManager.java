package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.stream.Stream;

import peterfajdiga.fastdraw.launcher.launcheritem.AppItem;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;
import peterfajdiga.fastdraw.launcher.launcheritem.ShortcutItem;

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

    public static void removePackageItems(final Context context, final Launcher pager, final String packageName, final boolean permanent) {
        for (final LauncherItem item : pager.getItems()) {
            if (packageName.equals(item.getPackageName()) && (permanent || !(item instanceof ShortcutItem))) {
                Log.i("AppItemManager", "Removing item: " + item.getID() + " of package " + item.getPackageName());
                pager.removeItem(item, permanent);
                if (item instanceof ShortcutItem) {
                    ShortcutItemManager.deleteShortcut(context, (ShortcutItem)item);
                }
            }
        }
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
