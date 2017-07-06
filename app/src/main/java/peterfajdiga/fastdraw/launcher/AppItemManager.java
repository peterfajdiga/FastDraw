package peterfajdiga.fastdraw.launcher;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;
import java.util.Map;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

public class AppItemManager {

    static final int ITEM_LOADER_LISTENER_ID = 0;

    private static void addAppItems(final Context context, final LauncherPager pager, final Intent launcherIntent) {
        final AsyncItemLoader.OnLoadCompleteListener<AppItem[]> itemLoaderListener = new AsyncItemLoader.OnLoadCompleteListener<AppItem[]>() {
            @Override
            public void onLoadComplete(Loader<AppItem[]> loader, AppItem[] data) {
                for (AppItem appItem : data) {
                    pager.addLauncherItem(appItem);
                }
            }
        };
        final AsyncItemLoader itemLoader = new AsyncItemLoader(context, launcherIntent);
        itemLoader.registerListener(ITEM_LOADER_LISTENER_ID, itemLoaderListener);
        itemLoader.onContentChanged();
        itemLoader.onStartLoading();
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
