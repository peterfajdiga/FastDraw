package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;

import peterfajdiga.fastdraw.launcher.LaunchManager;

public class OreoShortcutItem extends LauncherItem implements Saveable {
    private final String packageName;
    private final String oreoShortcutId;
    private final CharSequence label;
    private final Drawable icon;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public OreoShortcutItem(@NonNull final String packageName, @NonNull final String oreoShortcutId, @NonNull final CharSequence label, @NonNull final Drawable icon) {
        this.packageName = packageName;
        this.oreoShortcutId = oreoShortcutId;
        this.label = label;
        this.icon = icon;
    }

    @Override
    public String getID() {
        return "oreo\0" + (packageName + oreoShortcutId).hashCode(); // TODO: add salt?
    }

    @Override
    public CharSequence getLabel() {
        return label;
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void launch(final Context context, final LaunchManager launchManager, final View view) {
        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        @NonNull final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);

        if (!launcherApps.hasShortcutHostPermission()) {
            Log.w("OreoShortcutItem", "Fast Draw doesn't have shortcut host permission"); // TODO: toast
            return;
        }

        if (TextUtils.isEmpty(packageName)) {
            Log.e("OreoShortcutItem", "packageName is empty"); // TODO: toast?
            return;
        }

        final UserHandle user = getRunningUserHandle(launcherApps, userManager);
        if (user == null) {
            Log.e("OreoShortcutItem", "user is locked or not running"); // TODO: toast?
            return;
        }

        launcherApps.startShortcut(packageName, oreoShortcutId, view.getClipBounds(), null, user); // TODO: opts (animation)
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    private UserHandle getRunningUserHandle(@NonNull final LauncherApps launcherApps, @NonNull final UserManager userManager) {
        for (final UserHandle user : launcherApps.getProfiles()) {
            if (userManager.isUserRunning(user) && userManager.isUserUnlocked(user)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void toFile(@NonNull final File file) throws IOException {
        file.createNewFile(); // all data is in filename
    }

    @Override
    public String getTypeKey() {
        return "oreo-shortcuts";
    }

    @Override
    public String getFilename() {
        return getID().replace('\0', '_');
    }
}
