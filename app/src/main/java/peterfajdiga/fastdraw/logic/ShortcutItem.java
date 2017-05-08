package peterfajdiga.fastdraw.logic;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import peterfajdiga.fastdraw.activities.MainActivity;

public class ShortcutItem extends LauncherItem {

    private Intent intent;
    private boolean markedForDeletion = false;

    public ShortcutItem(Intent intent, String name, Drawable icon) {
        this.intent   = intent;
        this.name     = name;
        this.icon     = icon;
    }

    @Override
    public Intent getIntent() {
        return intent;
    }

    @Override
    public String getID() {
        return Integer.toString(intent.toUri(0).hashCode());
    }

    @Override
    public void persist(Context context) {
        if (markedForDeletion) {
            final File file = new File(getShortcutsDir(context), getID());
            file.delete();
        } else {
            try {
                toFile(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static File getShortcutsDir(Context context) {
        return new File(context.getFilesDir(), "shortcuts");
    }

    public static ShortcutItem shortcutFromIntent(Context context, Intent data) {
        Bitmap bmp = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
        Drawable icon = null;
        if (bmp != null) {
            icon = new BitmapDrawable(context.getResources(), bmp);
        } else {
            try {
                final Intent.ShortcutIconResource iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                final Resources resources = context.getPackageManager().getResourcesForApplication(iconResource.packageName);
                final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                icon = resources.getDrawable(id, context.getTheme());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ShortcutItem(
                (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                icon
        );
    }


    public void toFile(Context context) throws java.io.IOException {
        String uri = intent.toUri(0);
        FileOutputStream fos = new FileOutputStream(new File(getShortcutsDir(context), getID()));
        writeString(fos, uri);
        writeString(fos, name);
        writeString(fos, category);
        if (icon != null) {
            ((BitmapDrawable)icon).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        fos.close();
    }

    public static ShortcutItem fromFile(MainActivity context, File file) throws java.io.IOException, java.net.URISyntaxException {
        FileInputStream fis = new FileInputStream(file);

        Intent intent = Intent.parseUri(readString(fis), 0);
        String name = readString(fis);
        String category = readString(fis);

        ShortcutItem newItem = new ShortcutItem(
            intent,
            name,
            new BitmapDrawable(context.getResources(), BitmapFactory.decodeFileDescriptor(fis.getFD()))
        );
        newItem.setCategoryNoDirty(category, context);
        fis.close();
        return newItem;
    }

    public void remove(MainActivity context) {
        context.getPagerAdapter().removeLauncherItem(this, category);
        markedForDeletion = true;
        markAsDirty();
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
}
