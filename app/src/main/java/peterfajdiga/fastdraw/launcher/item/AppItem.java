package peterfajdiga.fastdraw.launcher.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.AppItemManager;

public class AppItem extends LauncherItem implements Loadable {

    public final String packageName;
    private final String activityName;

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
        return "app\0" + packageName + "\0" + activityName;
    }

    public void openAppDetails(final Context context) {
        AppItemManager.showPackageDetails(context, packageName);
    }


    /* loading */

    private ActivityInfo info;

    @Override
    public void load(@NonNull final Context context) {
        if (info != null) {
            final PackageManager packageManager = context.getPackageManager();
            name = info.loadLabel(packageManager).toString();
            icon = info.loadIcon(packageManager);
            info = null;
        }
    }

    @Override
    public boolean isLoaded() {
        return info == null;
    }
}
