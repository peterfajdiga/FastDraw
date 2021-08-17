package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.util.List;

import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Saveable;

public class AppItemManager {
    private AppItemManager() {}

    @NonNull
    private static AppItem[] getAppItems(@NonNull final PackageManager packageManager, @NonNull final Intent launcherIntent) {
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> appActivities = packageManager.queryIntentActivities(launcherIntent, 0);

        final AppItem[] apps = new AppItem[appActivities.size()];
        for (int i = 0; i < apps.length; i++) {
            apps[i] = new AppItem(appActivities.get(i).activityInfo);
        }

        return apps;
    }

    @NonNull
    public static AppItem[] getAppItems(@NonNull final PackageManager packageManager) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        return getAppItems(packageManager, launcherIntent);
    }

    @NonNull
    public static AppItem[] getAppItems(@NonNull final PackageManager packageManager, @NonNull final String packageName) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        launcherIntent.setPackage(packageName);
        return getAppItems(packageManager, launcherIntent);
    }

    public static void removePackageItems(final Context context, final Launcher pager, final String packageName, final boolean permanent) {
        for (final LauncherItem item : pager.getLauncherItems()) {
            if (packageName.equals(item.getPackageName()) && (permanent || !(item instanceof Saveable))) {
                pager.removeLauncherItem(item, permanent);
                if (item instanceof Saveable) {
                    ShortcutItemManager.deleteShortcut(context, (Saveable)item);
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
