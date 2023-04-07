package peterfajdiga.fastdraw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.ShortcutItemManager;
import peterfajdiga.fastdraw.launcher.launcheritem.FiledShortcutItem;

public class InstallShortcutReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent data) {
        final FiledShortcutItem newShortcutItem = ShortcutItemManager.shortcutFromIntent(context, data);
        ShortcutItemManager.saveShortcut(context, newShortcutItem);

        // TODO: Find out if it's necessary to save SHORTCUT category. Now it'll probably show up in LOST&FOUND if MainActivity == null
        // final String category = context.getString(R.string.default_shortcut_category);

        final MainActivity activity = MainActivity.getInstance();
        if (activity != null) {
            activity.onShortcutReceived(newShortcutItem);
        }
    }
}
