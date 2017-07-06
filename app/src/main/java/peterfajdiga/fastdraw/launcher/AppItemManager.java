package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

public class AppItemManager {

    private static class AsyncItemLoader extends Thread {
        private List<AppItem> appItems;
        private List<ResolveInfo> resInfoList;
        private PackageManager packageManager;

        AsyncItemLoader(final List<AppItem> appItems, final List<ResolveInfo> resInfoList, final PackageManager packageManager) {
            this.appItems = appItems;
            this.resInfoList = resInfoList;
            this.packageManager = packageManager;
        }

        @Override
        public void run() {
            final int n = appItems.size();
            for (int i = 0; i < n; i++) {
                final AppItem appItem = appItems.get(i);
                final ResolveInfo resInfo = resInfoList.get(i);
                appItem.name = resInfo.activityInfo.loadLabel(packageManager).toString();
                appItem.icon = resInfo.activityInfo.loadIcon(packageManager);
            }
        }
    }

    private static void addAppItems(final Context context, final LauncherPager pager, final Intent launcherIntent) {
        final SharedPreferences prefs = context.getSharedPreferences("categories", Context.MODE_PRIVATE);
        final PackageManager packageManager = context.getPackageManager();

        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> resInfoList = packageManager.queryIntentActivities(launcherIntent, 0);
        final List<AppItem> appItems = new ArrayList<>(resInfoList.size());
        for (final ResolveInfo resInfo : resInfoList) {
            final AppItem newAppItem = new AppItem(
                    resInfo.activityInfo.packageName,
                    resInfo.activityInfo.name,
                    "Loading...",
                    null
            );
            appItems.add(newAppItem);
            final String categoryName = prefs.getString(newAppItem.getID(), context.getString(R.string.default_category));
            newAppItem.setCategoryNoDirty(categoryName);
            pager.addLauncherItem(newAppItem);
        }
        final AsyncItemLoader itemLoader = new AsyncItemLoader(appItems, resInfoList, packageManager);
        itemLoader.start();
    }

    public static void addAppItems(final Context context, final LauncherPager pager) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        addAppItems(context, pager, launcherIntent);
    }

    public static void addAppItems(final Context context, final LauncherPager pager, final String packageName) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        launcherIntent.setPackage(packageName);
        addAppItems(context, pager, launcherIntent);
    }

    public static void removeAppItems(final LauncherPager pager, final String packageName) {
        final LauncherPagerAdapter adapter = pager.getAdapter();
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
}
