package peterfajdiga.fastdraw.launcher;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.AppItem;

public class AsyncItemLoader extends AsyncTaskLoader<AppItem[]> {
    private Intent launcherIntent;

    AsyncItemLoader(final Context context, final Intent launcherIntent) {
        super(context);
        this.launcherIntent = launcherIntent;
    }

    @Override
    public AppItem[] loadInBackground() {
        final Context context = getContext();
        final SharedPreferences prefs = context.getSharedPreferences("categories", Context.MODE_PRIVATE);
        final PackageManager packageManager = context.getPackageManager();

        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> resInfoList = packageManager.queryIntentActivities(launcherIntent, 0);
        final int n = resInfoList.size();
        final AppItem[] appItems = new AppItem[n];
        for (int i = 0; i < n; i++) {
            final ActivityInfo info = resInfoList.get(i).activityInfo;
            final AppItem newAppItem = new AppItem(
                    info.packageName,
                    info.name,
                    info.loadLabel(packageManager).toString(),
                    info.loadIcon(packageManager)
            );
            final String categoryName = prefs.getString(newAppItem.getID(), context.getString(R.string.default_category));
            newAppItem.setCategoryNoDirty(categoryName);
            appItems[i] = newAppItem;
        }
        return appItems;
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
