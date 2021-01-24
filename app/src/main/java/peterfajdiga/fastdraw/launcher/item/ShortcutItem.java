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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import peterfajdiga.fastdraw.R;

public class ShortcutItem extends LauncherItem implements Loadable {

    private final String label;
    private Drawable icon;
    private final Intent intent;
    private String iconPackageName = null;
    private String iconResourceName = null;
    private final String salt;

    public ShortcutItem(final Intent intent, final String salt, final String label, final Drawable icon) {
        this.intent = intent;
        this.salt = salt;
        this.label = label;
        this.icon = icon;
        this.isLoaded = true;
    }
    public ShortcutItem(final Intent intent, final String salt, final String label, final String iconPackageName, final String iconResourceName) {
        this(intent, salt, label, null);
        this.iconPackageName = iconPackageName;
        this.iconResourceName = iconResourceName;
        this.isLoaded = false;
    }

    @Override
    @NonNull
    public String getID() {
        return "shortcut\0" + intent.toUri(0).hashCode() + "\0" + salt;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @Override
    public Intent getIntent() {
        return intent;
    }

    private String getFilename() {
        return getID().replace('\0', '_');
    }

    public static File getShortcutsDir(@NonNull final Context context) {
        return new File(context.getFilesDir(), "shortcuts");
    }

    private static Drawable iconFromResource(final Context context, final String packageName, final String resourceName) throws PackageManager.NameNotFoundException {
        final Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
        final int id = resources.getIdentifier(resourceName, null, null);
        return ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
    }


    private static final String ICON_TYPE_NONE   = "n";
    private static final String ICON_TYPE_BITMAP = "b";
    private static final String ICON_TYPE_RES    = "r";
    public void toFile(@NonNull final Context context) throws java.io.IOException {
        final String uri = intent.toUri(0);
        final FileOutputStream fos = new FileOutputStream(new File(getShortcutsDir(context), getFilename())); // ID contains salt
        writeString(fos, uri);
        writeString(fos, label);
        if (iconResourceName != null) {
            writeString(fos, ICON_TYPE_RES);
            writeString(fos, iconPackageName);
            writeString(fos, iconResourceName);
        } else if (icon != null && icon instanceof BitmapDrawable) {
            writeString(fos, ICON_TYPE_BITMAP);
            ((BitmapDrawable)icon).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
        } else {
            writeString(fos, ICON_TYPE_NONE);
        }
        fos.close();
    }

    public static ShortcutItem fromFile(@NonNull final Context context, @NonNull final File file) throws java.io.IOException, java.net.URISyntaxException {
        final FileInputStream fis = new FileInputStream(file);

        final String filename = file.getName();
        final int saltIndex = filename.lastIndexOf('_') + 1;
        final String salt = filename.substring(saltIndex); // salt is in filename

        final Intent intent = Intent.parseUri(readString(fis), 0);
        final String name = readString(fis);

        return fromFileReadIcon(context, file, fis, intent, name, salt, true);
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
        final String iconType = readString(fis);
        switch (iconType) {
            case ICON_TYPE_BITMAP:
                return new ShortcutItem(intent, salt, name,
                    new BitmapDrawable(context.getResources(), BitmapFactory.decodeFileDescriptor(fis.getFD()))
                );
            case ICON_TYPE_RES:
                return new ShortcutItem(intent, salt, name, readString(fis), readString(fis));
            case ICON_TYPE_NONE:
                return new ShortcutItem(intent, salt, name, null);
            default:
                if (!tryOldFormat) {
                    return new ShortcutItem(intent, salt, name, null);
                }
                System.err.println("Try old format for file " + file.getAbsolutePath());
                // We have probably read category (from old version) instead of iconType.
                // In the old version, iconType followed category, so let's read again and this time we should get the iconType.
                final ShortcutItem item = fromFileReadIcon(context, file, fis, intent, name, salt, false);
                file.delete(); // delete the old file
                item.toFile(context); // save it in the new format
                System.err.println("Saved new format for file " + item.getFilename());
                return item;
        }
    }

    private static void writeString(@NonNull final FileOutputStream fos, @NonNull final String string) throws java.io.IOException {
        final byte[] stringBytes = string.getBytes();
        fos.write(ByteBuffer.allocate(4).putInt(stringBytes.length).array());
        fos.write(stringBytes);
    }

    private static String readString(@NonNull final InputStream fis) throws java.io.IOException {
        final byte[] stringLengthBytes = new byte[4];
        fis.read(stringLengthBytes);
        int stringLength = ByteBuffer.wrap(stringLengthBytes).getInt();

        final byte[] stringBytes = new byte[stringLength];
        fis.read(stringBytes);
        return new String(stringBytes);
    }


    /* loading */

    private boolean isLoaded;

    @Override
    public void load(@NonNull final Context context) {
        if (!isLoaded()) {
            try {
                icon = iconFromResource(context, iconPackageName, iconResourceName);
            } catch (final Exception e) {
                e.printStackTrace();
                ContextCompat.getDrawable(context, R.drawable.ic_item_shortcut_leftover);
            }
            isLoaded = true;
        }
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }
}
