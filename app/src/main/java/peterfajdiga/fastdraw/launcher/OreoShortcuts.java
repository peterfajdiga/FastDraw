package peterfajdiga.fastdraw.launcher;

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

import java.util.List;
import java.util.stream.Collectors;

import peterfajdiga.fastdraw.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class OreoShortcuts {
    private OreoShortcuts() {}

    @Nullable
    public static List<ShortcutInfo> getPinnedShortcuts(@NonNull final Context context) {
        final LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
        query.setQueryFlags(FLAG_MATCH_PINNED);
        return getShortcuts(context, query);
    }

    @Nullable
    public static List<ShortcutInfo> getPinnedShortcuts(
        @NonNull final Context context,
        @NonNull final String packageName
    ) {
        final LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
        query.setQueryFlags(FLAG_MATCH_PINNED);
        query.setPackage(packageName);
        return getShortcuts(context, query);
    }

    @Nullable
    private static List<ShortcutInfo> getShortcuts(
        @NonNull final Context context,
        @NonNull final LauncherApps.ShortcutQuery query
    ) {
        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        @NonNull final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);

        try {
            final UserHandle user = getRunningUserHandle(launcherApps, userManager);
            return launcherApps.getShortcuts(query, user);
        } catch (final IllegalStateException e) {
            Log.e("OreoShortcuts", "User is locked or not running (IllegalStateException)", e);
            Toast.makeText(context, R.string.error_oreo_user_handle, Toast.LENGTH_LONG).show();
            return null;
        } catch (final SecurityException e) {
            Log.e("OreoShortcuts", "Can't get shortcuts (SecurityException)", e);
            Toast.makeText(context, R.string.error_oreo_get_shortcuts, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static void unpinShortcut(
        @NonNull final Context context,
        @NonNull final String shortcutPackage,
        @NonNull final String shortcutId
    ) {
        final List<ShortcutInfo> pinnedShortcuts = getPinnedShortcuts(context, shortcutPackage);
        if (pinnedShortcuts == null) {
            Log.w("OreoShortcuts", String.format("Shortcut %s of package %s is not pinned", shortcutId, shortcutPackage));
            return;
        }

        final List<String> newPinnedShortcutIds = pinnedShortcuts.stream().
            map(ShortcutInfo::getId).
            filter(s -> !s.equals(shortcutId)).
            collect(Collectors.toList());

        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        @NonNull final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);

        try {
            final UserHandle user = getRunningUserHandle(launcherApps, userManager);
            launcherApps.pinShortcuts(shortcutPackage, newPinnedShortcutIds, user);
        } catch (final IllegalStateException e) {
            Log.e("OreoShortcuts", "User is locked or not running (IllegalStateException)", e);
            Toast.makeText(context, R.string.error_oreo_user_handle, Toast.LENGTH_LONG).show();
        } catch (final SecurityException e) {
            Log.e("OreoShortcuts", "Can't unpin shortcut (SecurityException)", e);
            Toast.makeText(context, R.string.error_oreo_get_shortcuts, Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    public static UserHandle getRunningUserHandle(
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

    public static CharSequence getLabel(@NonNull final ShortcutInfo shortcutInfo) {
        final CharSequence shortLabel = shortcutInfo.getShortLabel();
        if (!TextUtils.isEmpty(shortLabel)) {
            return shortLabel;
        }

        return shortcutInfo.getLongLabel();
    }

    public static Drawable getIcon(@NonNull final Context context, @NonNull ShortcutInfo shortcutInfo) {
        @NonNull final LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        return launcherApps.getShortcutIconDrawable(shortcutInfo, 0);
    }
}
