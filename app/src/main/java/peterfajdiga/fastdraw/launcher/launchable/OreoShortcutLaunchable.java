package peterfajdiga.fastdraw.launcher.launchable;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.LaunchManager;
import peterfajdiga.fastdraw.launcher.item.OreoShortcuts;

@RequiresApi(api = Build.VERSION_CODES.O)
public class OreoShortcutLaunchable implements Launchable {
    private final String packageName;
    private final String oreoShortcutId;

    public OreoShortcutLaunchable(final String packageName, final String oreoShortcutId) {
        this.packageName = packageName;
        this.oreoShortcutId = oreoShortcutId;
    }

    @Override
    public void launch(final Context context, final LaunchManager launchManager, final Bundle opts, final Rect clipBounds) {
        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        @NonNull final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);

        if (!launcherApps.hasShortcutHostPermission()) {
            Log.w("OreoShortcutItem", "Fast Draw doesn't have shortcut host permission");
            Toast.makeText(context, R.string.error_oreo_shortcut_host_permission, Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(packageName)) {
            Log.e("OreoShortcutItem", "packageName is empty");
            Toast.makeText(context, R.string.error_oreo_empty_package_name, Toast.LENGTH_LONG).show();
            return;
        }

        final UserHandle user = OreoShortcuts.getRunningUserHandle(launcherApps, userManager);
        if (user == null) {
            Log.e("OreoShortcutItem", "user is locked or not running");
            Toast.makeText(context, R.string.error_oreo_user_handle, Toast.LENGTH_LONG).show();
            return;
        }

        launcherApps.startShortcut(packageName, oreoShortcutId, clipBounds, opts, user);

    }
}
