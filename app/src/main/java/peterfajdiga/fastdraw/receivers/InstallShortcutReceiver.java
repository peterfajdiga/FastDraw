package peterfajdiga.fastdraw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.ShortcutItemManager;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class InstallShortcutReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent data) {
        System.err.println("asdf");
        final ShortcutItem newShortcutItem = ShortcutItem.shortcutFromIntent(context, data);
        ShortcutItemManager.saveShortcut(context, newShortcutItem);

        // TODO?: save SHORTCUT category? Now it'll probably show up in LOST&FOUND if MainActivity == null
        // final String category = context.getString(R.string.default_shortcut_category);

        final MainActivity activity = MainActivity.getInstance();
        if (activity != null) {
            activity.onShortcutReceived(newShortcutItem);
        }
    }
}
