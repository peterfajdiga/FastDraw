package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.IntentLaunchable;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

public class BitmapShortcutItem implements FiledShortcutItem {
    public static final String TYPE_KEY = "bitmap";

    private final String uuid;
    private final Intent intent;
    private final String label;
    private final BitmapDrawable icon;
    private final DisplayItem displayItem;

    public BitmapShortcutItem(
        final String uuid,
        final Intent intent,
        final String label,
        final BitmapDrawable icon
    ) {
        this.uuid = uuid;
        this.intent = intent;
        this.label = label;
        this.icon = icon;
        final Launchable launchable = new IntentLaunchable(intent);
        this.displayItem = new DisplayItem(getId(), label, icon, this, launchable);
    }

    @NonNull
    @Override
    public String getId() {
        return TYPE_KEY + "\0" + uuid;
    }

    @Nullable
    @Override
    public String getPackageName() {
        return intent.getPackage();
    }

    @NonNull
    @Override
    public DisplayItem getDisplayItem(final Context context) {
        return displayItem;
    }

    @Override
    public void save(final OutputStream out) throws IOException {
        final String uri = intent.toUri(0);
        Saveable.writeString(out, uri);
        Saveable.writeString(out, label);
        icon.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
    }

    @NonNull
    @Override
    public String getTypeKey() {
        return TYPE_KEY;
    }

    @NonNull
    @Override
    public String getUUID() {
        return uuid;
    }

    public static BitmapShortcutItem fromFile(
        @NonNull final Context context,
        @NonNull final FileInputStream in,
        @NonNull final String uuid
    ) throws java.io.IOException, java.net.URISyntaxException {
        final Intent intent = Intent.parseUri(Saveable.readString(in), 0);
        final String label = Saveable.readString(in);
        final BitmapDrawable icon = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFileDescriptor(in.getFD()));
        return new BitmapShortcutItem(uuid, intent, label, icon);
    }
}
