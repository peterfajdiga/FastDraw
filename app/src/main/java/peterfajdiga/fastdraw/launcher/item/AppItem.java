package peterfajdiga.fastdraw.launcher.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.LaunchManager;

public class AppItem extends LauncherItem implements Loadable {
    public static final String TYPE_KEY = "app";

    private String label;
    private Drawable icon;
    private final String packageName;
    private final String activityName;

    public AppItem(final ActivityInfo info) {
        packageName = info.packageName;
        activityName = info.name;
        this.info = info;
    }

    @Override
    @NonNull
    public String getID() {
        return TYPE_KEY + "\0" + packageName + "\0" + activityName;
    }

    @Override
    @NonNull
    public CharSequence getLabel() {
        if (isLoaded()) {
            return label;
        } else {
            return "Loading...";
        }
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @Override
    public void launch(
        final Context context,
        final LaunchManager launchManager,
        final Bundle opts,
        final Rect clipBounds
    ) {
        final Intent intent = getIntent();
        intent.setSourceBounds(clipBounds);
        launchManager.launch(intent, opts);
    }

    @NonNull
    private Intent getIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
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
            label = info.loadLabel(packageManager).toString();
            icon = info.loadIcon(packageManager);
            // Sometimes the label and/or icon returned here are garbled. TODO: Detect that and retry
            info = null;
        }
    }

    @Override
    public boolean isLoaded() {
        return info == null;
    }
}
