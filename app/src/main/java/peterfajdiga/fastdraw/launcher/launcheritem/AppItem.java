package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.IntentLaunchable;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

public class AppItem implements LauncherItem {
    public static final String TYPE_KEY = "app";

    private final ActivityInfo info;
    private DisplayItem displayItem = null;

    public AppItem(@NonNull final ActivityInfo info) {
        this.info = info;
    }

    @Override
    @NonNull
    public String getId() {
        return TYPE_KEY + "\0" + info.packageName + "\0" + info.name;
    }

    @NonNull
    private Intent getIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(info.packageName, info.name));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    @NonNull
    public String getPackageName() {
        return info.packageName;
    }

    @NonNull
    @Override
    public DisplayItem getDisplayItem(final Context context) {
        if (displayItem != null) {
            return displayItem;
        }

        final PackageManager packageManager = context.getPackageManager();
        final CharSequence label = info.loadLabel(packageManager).toString();
        final Drawable icon = info.loadIcon(packageManager);

        final Launchable launchable = new IntentLaunchable(getIntent());
        displayItem = new DisplayItem(getId(), label, icon, this, launchable);
        return displayItem;
    }

    public void openAppDetails(final Context context) {
        AppItemManager.showPackageDetails(context, info.packageName);
    }
}
