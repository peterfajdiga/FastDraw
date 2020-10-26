package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import peterfajdiga.fastdraw.R;

public class ShortcutItem extends LauncherItem implements Loadable {

    private final Intent intent;
    private String iconPackageName = null;
    private String iconResourceName = null;
    private final String salt;

    public ShortcutItem(final Intent intent, final String salt, final String name, final Drawable icon) {
        this.intent = intent;
        this.salt = salt;
        this.name = name;
        this.icon = icon;
    }
    public ShortcutItem(final Intent intent, final String salt, final String name, final String iconPackageName, final String iconResourceName) {
        this(intent, salt, name, null);
        this.iconPackageName = iconPackageName;
        this.iconResourceName = iconResourceName;
    }

    @Override
    public Intent getIntent() {
        return intent;
    }

    @Override
    public String getID() {
        return "shortcut\0" + intent.toUri(0).hashCode() + "\0" + salt;
    }

    private String getFilename() {
        return getID().replace('\0', '_');
    }

    private static String generateSalt() {
        return UUID.randomUUID().toString();
    }

    public static File getShortcutsDir(Context context) {
        return new File(context.getFilesDir(), "shortcuts");
    }

    public static ShortcutItem shortcutFromIntent(Context context, Intent data) {
        final Intent launchIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        final String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        final Bitmap bmp = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (bmp != null) {
            final Drawable icon = new BitmapDrawable(context.getResources(), bmp);
            return new ShortcutItem(launchIntent, generateSalt(), name, icon);
        } else {
            final Intent.ShortcutIconResource iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            return new ShortcutItem(launchIntent, generateSalt(), name, iconResource.packageName, iconResource.resourceName);
        }
    }
    private static Drawable iconFromResource(final Context context, final String packageName, final String resourceName) throws PackageManager.NameNotFoundException {
        final Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
        final int id = resources.getIdentifier(resourceName, null, null);
        return resources.getDrawable(id, context.getTheme());
    }


    private static final String ICON_TYPE_NONE   = "n";
    private static final String ICON_TYPE_BITMAP = "b";
    private static final String ICON_TYPE_RES    = "r";
    public void toFile(Context context) throws java.io.IOException {
        final String uri = intent.toUri(0);
        final FileOutputStream fos = new FileOutputStream(new File(getShortcutsDir(context), getFilename())); // ID contains salt
        writeString(fos, uri);
        writeString(fos, name);
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

    /*private void writeObject(@NonNull final java.io.ObjectOutputStream stream) throws java.io.IOException {
        stream.writeObject(intent.toUri(0));
    }*/

    public static ShortcutItem fromFile(Context context, File file) throws java.io.IOException, java.net.URISyntaxException {
        final FileInputStream fis = new FileInputStream(file);

        final String filename = file.getName();
        final int saltIndex = filename.lastIndexOf('_') + 1;
        final String salt = filename.substring(saltIndex); // salt is in filename

        final Intent intent = Intent.parseUri(readString(fis), 0);
        final String name = readString(fis);
        final String iconType = readString(fis);

        final ShortcutItem newItem;
        switch (iconType) {
            case ICON_TYPE_BITMAP: newItem = new ShortcutItem(intent, salt, name,
                new BitmapDrawable(context.getResources(), BitmapFactory.decodeFileDescriptor(fis.getFD()))
            ); break;
            case ICON_TYPE_RES:    newItem = new ShortcutItem(intent, salt, name, readString(fis), readString(fis)); break;
            default:               newItem = new ShortcutItem(intent, salt, name, null); break;
        }
        fis.close();
        return newItem;
    }

    private static void writeString(FileOutputStream fos, String string) throws java.io.IOException {
        byte[] stringBytes = string.getBytes();
        fos.write(ByteBuffer.allocate(4).putInt(stringBytes.length).array());
        fos.write(stringBytes);
    }

    private static String readString(FileInputStream fis) throws java.io.IOException {
        byte[] stringLengthBytes = new byte[4];
        fis.read(stringLengthBytes);
        int stringLength = ByteBuffer.wrap(stringLengthBytes).getInt();

        byte[] stringBytes = new byte[stringLength];
        fis.read(stringBytes);
        return new String(stringBytes);
    }


    /* loading */

    @Override
    public void load(final Context context) {
        if (iconResourceName != null) {
            try {
                icon = iconFromResource(context, iconPackageName, iconResourceName);
            } catch (Exception e) {
                ContextCompat.getDrawable(context, R.drawable.ic_item_shortcut_leftover);
            }
        }
    }
}
