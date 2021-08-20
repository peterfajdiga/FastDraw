package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.ShortcutItemManager;
import peterfajdiga.fastdraw.launcher.itemdisplay.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.IntentLaunchable;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

// TODO: split into two classes
public class ShortcutItem implements LauncherItem, Saveable {
    public static final String TYPE_KEY = "shortcut";

    private final String label;
    private Drawable icon;
    private final Intent intent;
    private String iconPackageName = null;
    private String iconResourceName = null;
    private final String salt;
    private DisplayItem displayItem = null;

    public ShortcutItem(final Intent intent, final String salt, final String label, final Drawable icon) {
        this.intent = intent;
        this.salt = salt;
        this.label = label;
        this.icon = icon;
    }
    public ShortcutItem(final Intent intent, final String salt, final String label, final String iconPackageName, final String iconResourceName) {
        this(intent, salt, label, null);
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

        loadIcon(context);

        final Launchable launchable = new IntentLaunchable(intent);
        displayItem = new DisplayItem(getID(), label, icon, launchable, this);
        return displayItem;
    }

    private static Drawable iconFromResource(final Context context, final String packageName, final String resourceName) throws PackageManager.NameNotFoundException, Resources.NotFoundException {
        final Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
        final int id = resources.getIdentifier(resourceName, null, null);
        return ResourcesCompat.getDrawable(resources, id, context.getTheme());
    }

    private static final String ICON_TYPE_NONE   = "n";
    private static final String ICON_TYPE_BITMAP = "b";
    private static final String ICON_TYPE_RES    = "r";

    @Override
    public void toFile(@NonNull final File file) throws java.io.IOException {
        final String uri = intent.toUri(0);
        final FileOutputStream fos = new FileOutputStream(file); // filename contains salt
        Saveable.writeString(fos, uri);
        Saveable.writeString(fos, label);
        if (iconResourceName != null) {
            Saveable.writeString(fos, ICON_TYPE_RES);
            Saveable.writeString(fos, iconPackageName);
            Saveable.writeString(fos, iconResourceName);
        } else if (icon != null && icon instanceof BitmapDrawable) {
            Saveable.writeString(fos, ICON_TYPE_BITMAP);
            ((BitmapDrawable)icon).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
        } else {
            Saveable.writeString(fos, ICON_TYPE_NONE);
        }
        fos.close();
    }

    @Override
    public String getFilename() {
        return getID().replace('\0', '_');
    }

    public static ShortcutItem fromFile(@NonNull final Context context, @NonNull final File file) throws java.io.IOException, java.net.URISyntaxException {
        final FileInputStream fis = new FileInputStream(file);

        final String filename = file.getName();
        final int saltIndex = filename.lastIndexOf('_') + 1;
        final String salt = filename.substring(saltIndex); // salt is in filename

        final Intent intent = Intent.parseUri(Saveable.readString(fis), 0);
        final String name = Saveable.readString(fis);

        final ShortcutItem shortcutItem = fromFileReadIcon(context, file, fis, intent, name, salt, true);
        fis.close();
        return shortcutItem;
    }

    private static ShortcutItem fromFileReadIcon(
        @NonNull final Context context,
        @NonNull final File file,
        @NonNull final FileInputStream fis,
        @NonNull final Intent intent,
        @NonNull final String name,
        @NonNull final String salt,
        final boolean tryOldFormat
    ) throws java.io.IOException {
        final String iconType = Saveable.readString(fis);
        switch (iconType) {
            case ICON_TYPE_BITMAP:
                return new ShortcutItem(intent, salt, name,
                    new BitmapDrawable(context.getResources(), BitmapFactory.decodeFileDescriptor(fis.getFD()))
                );
            case ICON_TYPE_RES:
                return new ShortcutItem(intent, salt, name, Saveable.readString(fis), Saveable.readString(fis));
            case ICON_TYPE_NONE:
                return new ShortcutItem(intent, salt, name, null);
            default:
                if (!tryOldFormat) {
                    return new ShortcutItem(intent, salt, name, null);
                }
                // We have probably read the category name (from the old format) instead of iconType.
                // In the old format, iconType followed category, so let's read again and this time we should get the iconType.
                final ShortcutItem item = fromFileReadIcon(context, file, fis, intent, name, salt, false);
                file.delete(); // delete the old file
                ShortcutItemManager.saveShortcut(context, item); // save it in the new format
                return item;
        }
    }

    private void loadIcon(@NonNull final Context context) {
        if (icon != null) {
            return;
        }

        try {
            try {
                icon = iconFromResource(context, iconPackageName, iconResourceName);
            } catch (final Resources.NotFoundException e) {
                icon = context.getPackageManager().getApplicationIcon(iconPackageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_item_shortcut_leftover);
        }
    }
}
