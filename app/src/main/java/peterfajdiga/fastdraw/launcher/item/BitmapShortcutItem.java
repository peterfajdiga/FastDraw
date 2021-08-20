package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import peterfajdiga.fastdraw.launcher.itemdisplay.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.IntentLaunchable;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

public class BitmapShortcutItem implements LauncherItem, Saveable {
    public static final String TYPE_KEY = "shortcut";
    public static final String ICON_TYPE_NONE   = "n";
    public static final String ICON_TYPE_BITMAP = "b";

    private final Intent intent;
    private final String salt;
    private final DisplayItem displayItem;

    public BitmapShortcutItem(final Intent intent, final String salt, final String label, final Drawable icon) {
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
    public void toFile(final File file) throws IOException {
        final String uri = intent.toUri(0);
        final FileOutputStream fos = new FileOutputStream(file); // filename contains salt
        Saveable.writeString(fos, uri);
        Saveable.writeString(fos, displayItem.getLabel().toString());
        final Drawable icon = displayItem.getIcon();
        if (icon instanceof BitmapDrawable) {
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
}
