package peterfajdiga.fastdraw.launcher.launcheritem;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.InputStream;
import java.io.OutputStream;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.launcher.launchable.IntentLaunchable;
import peterfajdiga.fastdraw.launcher.launchable.Launchable;

public class ResShortcutItem implements ShortcutItem {
    public static final String TYPE_KEY = "res";

    private final String uuid;
    private final Intent intent;
    private final String label;
    private final String iconPackageName;
    private final String iconResourceName;
    private DisplayItem displayItem = null;

    public ResShortcutItem(
        final String uuid,
        final Intent intent,
        final String label,
        final String iconPackageName,
        final String iconResourceName
    ) {
        this.uuid = uuid;
        this.intent = intent;
        this.label = label;
        this.iconPackageName = iconPackageName;
        this.iconResourceName = iconResourceName;
    }

    @Override
    @NonNull
    public String getId() {
        return TYPE_KEY + "\0" + uuid;
    }

    @Override
    @Nullable
    public String getPackageName() {
        return intent.getPackage();
    }

    @NonNull
    @Override
    public DisplayItem getDisplayItem(final Context context) {
        if (displayItem != null) {
            return displayItem;
        }

        final Drawable icon = loadIcon(context);
        final Launchable launchable = new IntentLaunchable(intent);
        displayItem = new DisplayItem(getId(), label, icon, this, launchable);
        return displayItem;
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

    private static Drawable iconFromResource(
        final Context context,
        final String packageName,
        final String resourceName
    ) throws PackageManager.NameNotFoundException, Resources.NotFoundException {
        final Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
        final int id = resources.getIdentifier(resourceName, null, null);
        return ResourcesCompat.getDrawable(resources, id, context.getTheme());
    }

    @Override
    public void save(@NonNull final OutputStream out) throws java.io.IOException {
        final String uri = intent.toUri(0);
        Saveable.writeString(out, uri);
        Saveable.writeString(out, label);
        Saveable.writeString(out, iconPackageName);
        Saveable.writeString(out, iconResourceName);
    }

    public static ResShortcutItem fromFile(
        @NonNull final Context context,
        @NonNull final InputStream in,
        @NonNull final String uuid
    ) throws java.io.IOException, java.net.URISyntaxException {
        final Intent intent = Intent.parseUri(Saveable.readString(in), 0);
        final String label = Saveable.readString(in);
        final String iconPackageName = Saveable.readString(in);
        final String iconResourceName = Saveable.readString(in);
        return new ResShortcutItem(uuid, intent, label, iconPackageName, iconResourceName);
    }
}
