package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.itemdisplay.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.IntentLaunchable;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

// TODO: split into two classes
public class ResShortcutItem implements LauncherItem, Saveable {
    public static final String TYPE_KEY = "shortcut";
    public static final String ICON_TYPE_RES = "r";

    private final String label;
    private final String iconPackageName;
    private final String iconResourceName;
    private final Intent intent;
    private final String salt;
    private DisplayItem displayItem = null;

    public ResShortcutItem(final Intent intent, final String salt, final String label, final String iconPackageName, final String iconResourceName) {
        this.intent = intent;
        this.salt = salt;
        this.label = label;
        this.iconPackageName = iconPackageName;
        this.iconResourceName = iconResourceName;
    }

    @Override
    @NonNull
    public String getID() {
        return TYPE_KEY + "\0" + intent.toUri(0).hashCode() + "\0" + salt;
    }

    @Override
    @Nullable
    public String getPackageName() {
        return intent.getPackage();
    }

    @Override
    public DisplayItem getDisplayItem(final Context context) {
        if (displayItem != null) {
            return displayItem;
        }

        final Drawable icon = loadIcon(context);
        final Launchable launchable = new IntentLaunchable(intent);
        displayItem = new DisplayItem(getID(), label, icon, launchable, this);
        return displayItem;
    }

    private static Drawable iconFromResource(final Context context, final String packageName, final String resourceName) throws PackageManager.NameNotFoundException, Resources.NotFoundException {
        final Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
        final int id = resources.getIdentifier(resourceName, null, null);
        return ResourcesCompat.getDrawable(resources, id, context.getTheme());
    }

    @Override
    public void toFile(@NonNull final File file) throws java.io.IOException {
        final String uri = intent.toUri(0);
        final FileOutputStream fos = new FileOutputStream(file); // filename contains salt
        Saveable.writeString(fos, uri);
        Saveable.writeString(fos, label);
        Saveable.writeString(fos, ICON_TYPE_RES);
        Saveable.writeString(fos, iconPackageName);
        Saveable.writeString(fos, iconResourceName);
        fos.close();
    }

    @Override
    public String getFilename() {
        return getID().replace('\0', '_');
    }

    private Drawable loadIcon(@NonNull final Context context) {
        try {
            try {
                return iconFromResource(context, iconPackageName, iconResourceName);
            } catch (final Resources.NotFoundException e) {
                return context.getPackageManager().getApplicationIcon(iconPackageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_item_shortcut_leftover);
        }
    }
}
