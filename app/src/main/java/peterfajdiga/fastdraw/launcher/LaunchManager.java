package peterfajdiga.fastdraw.launcher;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

public class LaunchManager {
    public static final int PERMISSION_REQUEST_CODE = 42;

    private final Activity activity;
    private DelayedLaunch delayedLaunch = null;

    public LaunchManager(@NonNull final Activity activity) {
        this.activity = activity;
    }

    public void launch(@NonNull final LauncherItem item, @NonNull final View view) {
        Intent intent = item.getIntent();

        if (intent == null) {
            final String errMessage = activity.getString(R.string.no_intent);
            final Toast toast = Toast.makeText(activity, errMessage, Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // animation
        ActivityOptions opts;
        if (Build.VERSION.SDK_INT >= 23) {
            opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());
        } else {
            opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
        }

        switch (intent.getAction()) {
            case Intent.ACTION_CALL:
                launchWithPermission(intent, opts, Manifest.permission.CALL_PHONE);
                break;
            default:
                launch(intent, opts.toBundle());
                break;
        }
    }

    private void launch(@NonNull final Intent intent, @NonNull final Bundle opts) {
        try {
            activity.startActivity(intent, opts);
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            final String errMessage = activity.getString(R.string.no_app);
            final Toast toast = Toast.makeText(activity, errMessage, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void launchWithPermission(@NonNull final Intent launchIntent, @NonNull final ActivityOptions launchOpts, @NonNull final String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
        delayedLaunch = new DelayedLaunch(launchIntent, launchOpts.toBundle());
    }

    public void onRequestPermissionsResult(@NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (delayedLaunch == null) {
            return;
        }

        if (gotPermission(permissions, grantResults)) {
            launch(delayedLaunch.launchIntent, delayedLaunch.launchOpts);
        } else {
            final String errMessage = activity.getString(R.string.no_permission);
            final Toast toast = Toast.makeText(activity, errMessage, Toast.LENGTH_LONG);
            toast.show();
        }

        delayedLaunch = null;
    }

    private boolean gotPermission(@NonNull final String[] permissions, @NonNull final int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private static class DelayedLaunch {
        private final Intent launchIntent;
        private final Bundle launchOpts;

        public DelayedLaunch(@NonNull final Intent launchIntent, @NonNull final Bundle launchOpts) {
            this.launchIntent = launchIntent;
            this.launchOpts = launchOpts;
        }
    }
}
