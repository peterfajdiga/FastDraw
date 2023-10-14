package peterfajdiga.fastdraw.launcher;

import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import peterfajdiga.fastdraw.R;

public class OreoShortcuts {
    private OreoShortcuts() {}

    public static CharSequence getLabel(@NonNull final ShortcutInfo shortcutInfo) {
        final CharSequence shortLabel = shortcutInfo.getShortLabel();
        if (!TextUtils.isEmpty(shortLabel)) {
            return shortLabel;
        }

        return shortcutInfo.getLongLabel();
    }

    public static Drawable getIcon(@NonNull final Context context, @NonNull ShortcutInfo shortcutInfo) {
        final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        return launcherApps.getShortcutIconDrawable(shortcutInfo, 0);
    }

    @Nullable
    public static List<ShortcutInfo> getPinnedShortcuts(@NonNull final Context context) {
        final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);
        try {
            final UserHandle userHandle = getRunningUserHandle(launcherApps, userManager);
            return getPinnedShortcuts(launcherApps, userHandle);
        } catch (final IllegalStateException e) {
            handleLauncherAppsException(context, e);
        } catch (final SecurityException e) {
            handleLauncherAppsException(context, e);
        }
        return null;
    }

    public static void unpinShortcut(
        @NonNull final Context context,
        @NonNull final String shortcutPackage,
        @NonNull final String shortcutId
    ) {
        final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);
        try {
            final UserHandle userHandle = getRunningUserHandle(launcherApps, userManager);
            unpinShortcut(launcherApps, userHandle, shortcutPackage, shortcutId);
        } catch (final IllegalStateException e) {
            handleLauncherAppsException(context, e);
        } catch (final SecurityException e) {
            handleLauncherAppsException(context, e);
        }
    }

    @Nullable
    private static List<ShortcutInfo> getPinnedShortcuts(
        @NonNull final LauncherApps launcherApps,
        @NonNull final UserHandle user
    ) throws IllegalStateException, SecurityException {
        final LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
        query.setQueryFlags(FLAG_MATCH_PINNED);
        return launcherApps.getShortcuts(query, user);
    }

    @Nullable
    private static List<ShortcutInfo> getPinnedShortcuts(
        @NonNull final LauncherApps launcherApps,
        @NonNull final UserHandle user,
        @NonNull final String packageName
    ) throws IllegalStateException, SecurityException {
        final LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
        query.setQueryFlags(FLAG_MATCH_PINNED);
        query.setPackage(packageName);
        return launcherApps.getShortcuts(query, user);
    }

    private static void unpinShortcut(
        @NonNull final LauncherApps launcherApps,
        @NonNull final UserHandle user,
        @NonNull final String shortcutPackage,
        @NonNull final String shortcutId
    ) throws IllegalStateException, SecurityException {
        final List<ShortcutInfo> pinnedShortcuts = getPinnedShortcuts(launcherApps, user, shortcutPackage);
        if (pinnedShortcuts == null) {
            Log.w("OreoShortcuts", String.format("Shortcut %s of package %s is not pinned", shortcutId, shortcutPackage));
            return;
        }

        final List<String> newPinnedShortcutIds = pinnedShortcuts.stream().
            map(ShortcutInfo::getId).
            filter(id -> !id.equals(shortcutId)).
            collect(Collectors.toList());

        launcherApps.pinShortcuts(shortcutPackage, newPinnedShortcutIds, user);
    }

    @NonNull
    private static UserHandle getRunningUserHandle(
        @NonNull final LauncherApps launcherApps,
        @NonNull final UserManager userManager
    ) throws IllegalStateException {
        for (final UserHandle user : launcherApps.getProfiles()) {
            if (userManager.isUserRunning(user) && userManager.isUserUnlocked(user)) {
                return user;
            }
        }
        throw new IllegalStateException("no unlocked running user");
    }

    private static void handleLauncherAppsException(
        @NonNull final Context context,
        @NonNull final IllegalStateException e
    ) {
        Log.e("OreoShortcuts", "User is locked or not running (IllegalStateException)", e);
        Toast.makeText(context, R.string.error_oreo_user_handle, Toast.LENGTH_LONG).show();
    }

    private static void handleLauncherAppsException(
        @NonNull final Context context,
        @NonNull final SecurityException e
    ) {
        Log.e("OreoShortcuts", "Can't access shortcuts (SecurityException)", e);
        Toast.makeText(context, R.string.error_oreo_shortcuts_security, Toast.LENGTH_LONG).show();
    }
}
