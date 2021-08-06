package peterfajdiga.fastdraw.launcher.item;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.LaunchManager;

public class AppItem extends LauncherItem implements Loadable {

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
        return "app\0" + packageName + "\0" + activityName;
    }

    @Override
    @NonNull
    public String getLabel() {
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
    public void launch(final LaunchManager launchManager, final View view) {
        final Intent intent = getIntent();

        // animation // TODO: deduplicate code
        ActivityOptions opts;
        if (Build.VERSION.SDK_INT >= 23) {
            opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());
        } else {
            opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
        }

        launchManager.launch(intent, opts.toBundle());
    }

    @NonNull
    public Intent getIntent() {
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
            info = null;
        }
    }

    @Override
    public boolean isLoaded() {
        return info == null;
    }
}
