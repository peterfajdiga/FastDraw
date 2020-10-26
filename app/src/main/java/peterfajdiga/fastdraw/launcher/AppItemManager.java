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

import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

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

    public static void removeAppItems(final Context context, final LauncherPager pager, final String packageName) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)pager.getAdapter();
        for (Map.Entry categoryEntry : adapter.categoryViews.entrySet()) {
            final String categoryName = (String)categoryEntry.getKey();
            final CategoryView categoryView = (CategoryView)categoryEntry.getValue();
            final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter)categoryView.getAdapter();

            boolean itemsRemoved = false;
            // iterate through categories' items
            for (int i = 0; i < innerAdapter.getCount();) {
                final LauncherItem launcherItem = innerAdapter.getItem(i);
                if (launcherItem instanceof AppItem && ((AppItem)launcherItem).packageName.equals(packageName)) {
                    // remove matching items
                    // don't increment i in this case, as item count has decreased
                    innerAdapter.remove(launcherItem);
                    // TODO: refactor: call LauncherPager.removeLauncherItem instead
                    // TODO: refactor: maybe remove the now unused categories prefmap key here
                    itemsRemoved = true;
                } else {
                    i++;
                }
            }
            if (itemsRemoved) {
                // update category adapter
                innerAdapter.notifyDataSetChanged();
                if (innerAdapter.getCount() == 0) {
                    // remove the now empty category from pager
                    adapter.categoryViews.remove(categoryName);
                }
            }
        }
        adapter.notifyDataSetChanged();
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
