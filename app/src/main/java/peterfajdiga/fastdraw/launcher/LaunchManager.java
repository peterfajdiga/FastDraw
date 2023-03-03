package peterfajdiga.fastdraw.launcher;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import peterfajdiga.fastdraw.R;

public class LaunchManager {
    public static final int PERMISSION_REQUEST_CODE = 42;

    private final Activity activity;
    private DelayedLaunch delayedLaunch = null;

    public LaunchManager(@NonNull final Activity activity) {
        this.activity = activity;
    }

    public void launch(final Intent intent, @NonNull final Bundle opts) {
        if (intent == null) {
            final Toast toast = Toast.makeText(activity, R.string.no_intent, Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        switch (intent.getAction()) {
            case Intent.ACTION_CALL:
                launchWithPermission(intent, opts, Manifest.permission.CALL_PHONE);
                break;
            default:
                launchWithoutPermission(intent, opts);
                break;
        }
    }

    private void launchWithoutPermission(@NonNull final Intent intent, @NonNull final Bundle opts) {
        try {
            activity.startActivity(intent, opts);
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            final Toast toast = Toast.makeText(activity, R.string.no_app, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void launchWithPermission(@NonNull final Intent launchIntent, @NonNull final Bundle opts, @NonNull final String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
        delayedLaunch = new DelayedLaunch(launchIntent, opts);
    }

    public void onRequestPermissionsResult(@NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (delayedLaunch == null) {
            return;
        }

        if (gotPermission(permissions, grantResults)) {
            launch(delayedLaunch.launchIntent, delayedLaunch.launchOpts);
        } else {
            final Toast toast = Toast.makeText(activity, R.string.no_permission, Toast.LENGTH_LONG);
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
