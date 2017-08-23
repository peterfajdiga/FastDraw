package peterfajdiga.fastdraw.launcher.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.launcher.AppItemManager;

public class AppItem extends LauncherItem {

    public String packageName;
    private String activityName;

    public AppItem(final ActivityInfo info) {
        packageName = info.packageName;
        activityName = info.name;
        this.info = info;
    }

    @Override
    public Intent getIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public String getID() {
        return packageName + "\0" + activityName;
    }

    @Override
    public void persist(final Context context) {
        final PrefMap categories = new PrefMap(context, "categories");
        categories.putString(getID(), category);
    }

    public void openAppDetails(final Context context) {
        AppItemManager.showPackageDetails(context, packageName);
    }


    /* loading */

    private ActivityInfo info;

    @Override
    public void load(final Context context) {
        if (info != null) {
            final PackageManager packageManager = context.getPackageManager();
            name = info.loadLabel(packageManager).toString();
            icon = info.loadIcon(packageManager);

            // forget
            info = null;
        }
    }
}
