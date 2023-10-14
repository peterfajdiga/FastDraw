package peterfajdiga.fastdraw.launcher.launchable;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.LaunchManager;

public class OreoShortcutLaunchable implements Launchable {
    private final ShortcutInfo info;

    public OreoShortcutLaunchable(@NonNull final ShortcutInfo info) {
        this.info = info;
    }

    @Override
    public void launch(final Context context, final LaunchManager launchManager, final Bundle opts, final Rect clipBounds) {
        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        if (!launcherApps.hasShortcutHostPermission()) {
            Log.w("OreoShortcutLaunchable", "Fast Draw doesn't have shortcut host permission");
            Toast.makeText(context, R.string.error_oreo_shortcut_host_permission, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            launcherApps.startShortcut(info, clipBounds, opts);
        } catch (final IllegalStateException e) {
            Log.e("OreoShortcutLaunchable", "IllegalStateException when trying to launch shortcut " + info.getId() + " from package " + info.getPackage(), e);
            Toast.makeText(context, R.string.error_oreo_user_handle, Toast.LENGTH_LONG).show();
        } catch (final ActivityNotFoundException e) {
            Log.e("OreoShortcutLaunchable", "ActivityNotFoundException when trying to launch shortcut " + info.getId() + " from package " + info.getPackage(), e);
            Toast.makeText(context, R.string.error_oreo_activity_not_found, Toast.LENGTH_LONG).show();
        }
    }
}
