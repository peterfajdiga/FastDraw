package peterfajdiga.fastdraw.launcher.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import peterfajdiga.fastdraw.launcher.AppItemManager;

public class AppItem extends LauncherItem {

    public String packageName;
    private String activityName;

    public AppItem(final ActivityInfo info, final PackageManager packageManager) {
        packageName = info.packageName;
        activityName = info.name;
        this.info = info;
        this.packageManager = packageManager;
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
        final SharedPreferences prefs = context.getSharedPreferences("categories", Context.MODE_PRIVATE);
        final SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(getID(), category);
        prefsEditor.apply();
    }

    public void openAppDetails(final Context context) {
        AppItemManager.showPackageDetails(context, packageName);
    }


    /* loading */

    private ActivityInfo info;
    private PackageManager packageManager;

    @Override
    public void load() {
        if (info != null) {
            name = info.loadLabel(packageManager).toString();
            icon = info.loadIcon(packageManager);

            // forget
            info = null;
            packageManager = null;
        }
    }
}
