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
        final MainActivity activity = MainActivity.getInstance();

        if (activity != null) {
            final FiledShortcutItem newShortcutItem = activity.shortcutItemManager.shortcutFromIntent(context, data);
            activity.shortcutItemManager.saveShortcut(context, newShortcutItem);

            activity.onShortcutReceived(newShortcutItem);
        }
    }
}
