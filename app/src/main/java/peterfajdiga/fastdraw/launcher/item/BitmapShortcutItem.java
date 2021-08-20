package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import peterfajdiga.fastdraw.launcher.itemdisplay.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.IntentLaunchable;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

public class BitmapShortcutItem implements LauncherItem, Saveable {
    public static final String TYPE_KEY = "bitmap";

    private final BitmapDrawable icon;
    private final Intent intent;
    private final String salt;
    private final DisplayItem displayItem;

    public BitmapShortcutItem(final Intent intent, final String salt, final String label, final BitmapDrawable icon) {
        this.icon = icon;
        this.intent = intent;
        this.salt = salt;
        final Launchable launchable = new IntentLaunchable(intent);
        this.displayItem = new DisplayItem(getID(), label, icon, launchable, this);
    }

    @Override
    public String getID() {
        return TYPE_KEY + "\0" + intent.toUri(0).hashCode() + "\0" + salt;
    }

    @Nullable
    @Override
    public String getPackageName() {
        return intent.getPackage();
    }

    @Override
    public DisplayItem getDisplayItem(final Context context) {
        return displayItem;
    }

    @Override
    public String getFilename() {
        return getID().replace('\0', '_');
    }

    @Override
    public void toFile(final File file) throws IOException {
        final String uri = intent.toUri(0);
        final FileOutputStream fos = new FileOutputStream(file); // filename contains salt
        Saveable.writeString(fos, uri);
        Saveable.writeString(fos, displayItem.getLabel().toString());
        icon.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
    }

    public static BitmapShortcutItem fromFile(@NonNull final Context context, @NonNull final File file) throws java.io.IOException, java.net.URISyntaxException {
        final FileInputStream fis = new FileInputStream(file);

        final String filename = file.getName();
        final int saltIndex = filename.lastIndexOf('_') + 1;
        final String salt = filename.substring(saltIndex); // salt is in filename

        final Intent intent = Intent.parseUri(Saveable.readString(fis), 0);
        final String label = Saveable.readString(fis);

        final BitmapDrawable icon = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFileDescriptor(fis.getFD()));
        fis.close();

        return new BitmapShortcutItem(intent, salt, label, icon);
    }
}
