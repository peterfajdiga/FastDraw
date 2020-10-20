package peterfajdiga.fastdraw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.ItemPersistence;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class InstallShortcutReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent data) {
        System.err.println("asdf");
        final ShortcutItem newShortcutItem = ShortcutItem.shortcutFromIntent(context, data);
        final String category = context.getString(R.string.default_shortcut_category);
        ItemPersistence.persistItem(context, newShortcutItem, category); // TODO: refactor
        // TODO?: save SHORTCUT category? Now it'll probably show up in LOST&FOUND

        final MainActivity activity = MainActivity.getInstance();
        if (activity != null) {
            activity.onShortcutReceived(newShortcutItem);
        }
    }
}
