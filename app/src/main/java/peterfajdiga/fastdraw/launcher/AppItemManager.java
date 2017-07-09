package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import java.util.Map;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

public class AppItemManager {

    private static void addAppItems(final Context context, final LauncherPager pager, final Intent launcherIntent, boolean delayLoad) {
        final SharedPreferences prefs = context.getSharedPreferences("categories", Context.MODE_PRIVATE);
        final PackageManager packageManager = context.getPackageManager();

        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (final ResolveInfo resInfo : packageManager.queryIntentActivities(launcherIntent, 0)) {
            final AppItem newAppItem = new AppItem(resInfo.activityInfo, packageManager);
            if (!delayLoad) {
                newAppItem.load();
            }
            final String categoryName = prefs.getString(newAppItem.getID(), context.getString(R.string.default_category));
            newAppItem.setCategoryNoDirty(categoryName);
            pager.addLauncherItemBulk(newAppItem);
        }
        pager.finishBulk();
    }

    public static void addAppItems(final Context context, final LauncherPager pager, boolean delayLoad) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        addAppItems(context, pager, launcherIntent, delayLoad);
    }

    public static void addAppItems(final Context context, final LauncherPager pager, final String packageName, boolean delayLoad) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        launcherIntent.setPackage(packageName);
        addAppItems(context, pager, launcherIntent, delayLoad);
    }

    public static void removeAppItems(final Context context, final LauncherPager pager, final String packageName) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)pager.getAdapter();
        for (Map.Entry categoryEntry : adapter.categories.entrySet()) {
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
                    launcherItem.persist(context);
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
                    adapter.categories.remove(categoryName);
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
