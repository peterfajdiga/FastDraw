package peterfajdiga.fastdraw.launcher;

import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Collections;
import java.util.List;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.launcheritem.Saveable;

public class OreoShortcuts {
    private OreoShortcuts() {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public static ShortcutInfo getShortcutInfo(
        @NonNull final Context context,
        @NonNull final String packageName,
        @NonNull final String oreoShortcutId
    ) throws Saveable.LeftoverException {
        final LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
        query.setPackage(packageName);
        query.setShortcutIds(Collections.singletonList(oreoShortcutId));
        query.setQueryFlags(FLAG_MATCH_DYNAMIC | FLAG_MATCH_MANIFEST | FLAG_MATCH_PINNED);

        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        @NonNull final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);

        final UserHandle user = getRunningUserHandle(launcherApps, userManager);
        if (user == null) {
            Log.e("OreoShortcuts", "User is locked or not running");
            Toast.makeText(context, R.string.error_oreo_user_handle, Toast.LENGTH_LONG).show();
            return null;
        }

        final List<ShortcutInfo> shortcuts;
        try {
            shortcuts = launcherApps.getShortcuts(query, user);
        } catch (final IllegalStateException e) {
            Log.e("OreoShortcuts", "User is locked or not running (IllegalStateException)", e);
            Toast.makeText(context, R.string.error_oreo_user_handle, Toast.LENGTH_LONG).show();
            return null;
        } catch (final SecurityException e) {
            Log.e("OreoShortcuts", "Can't get shortcuts (SecurityException)", e);
            Toast.makeText(context, R.string.error_oreo_get_shortcuts, Toast.LENGTH_LONG).show();
            return null;
        }

        if (shortcuts.isEmpty()) {
            Log.e("OreoShortcuts", "No shortcuts found");
            throw new Saveable.LeftoverException();
        }

        return shortcuts.get(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public static UserHandle getRunningUserHandle(@NonNull final LauncherApps launcherApps, @NonNull final UserManager userManager) {
        for (final UserHandle user : launcherApps.getProfiles()) {
            if (userManager.isUserRunning(user) && userManager.isUserUnlocked(user)) {
                return user;
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static CharSequence getLabel(@NonNull final ShortcutInfo shortcutInfo) {
        final CharSequence shortLabel = shortcutInfo.getShortLabel();
        if (!TextUtils.isEmpty(shortLabel)) {
            return shortLabel;
        }

        return shortcutInfo.getLongLabel();
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static Drawable getIcon(@NonNull final Context context, @NonNull ShortcutInfo shortcutInfo) {
        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        return launcherApps.getShortcutIconDrawable(shortcutInfo, 0);
    }
}
