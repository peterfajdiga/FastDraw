package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.LaunchManager;

public class OreoShortcutItem extends LauncherItem implements Saveable {
    public static final String TYPE_KEY = "oreo";

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
        return TYPE_KEY + "\0" + (packageName + oreoShortcutId).hashCode(); // TODO: add salt?
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
    public void launch(
        final Context context,
        final LaunchManager launchManager,
        final Bundle opts,
        final Rect clipBounds
    ) {
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

    @Override
    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @Override
    public void toFile(@NonNull final File file) throws IOException {
        final FileOutputStream fos = new FileOutputStream(file);
        Saveable.writeString(fos, packageName);
        Saveable.writeString(fos, oreoShortcutId);
        fos.close();
    }

    @Override
    public String getFilename() {
        return getID().replace('\0', '_');
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public static OreoShortcutItem fromFile(@NonNull final Context context, @NonNull final File file) throws java.io.IOException, LeftoverException {
        final FileInputStream fis = new FileInputStream(file);
        final String packageName = Saveable.readString(fis);
        final String oreoShortcutId = Saveable.readString(fis);
        fis.close();

        final ShortcutInfo shortcutInfo = OreoShortcuts.getShortcutInfo(context, packageName, oreoShortcutId);
        if (shortcutInfo == null) {
            return null;
        }

        return new OreoShortcutItem(
            packageName,
            oreoShortcutId,
            OreoShortcuts.getLabel(shortcutInfo),
            OreoShortcuts.getIcon(context, shortcutInfo)
        );
    }
}
