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
        final ShortcutItem newShortcutItem = ShortcutItem.shortcutFromIntent(context, data);
        final String category = context.getString(R.string.default_shortcut_category);
        newShortcutItem.setCategory(category);
        final MainActivity activity = MainActivity.getInstance();
        if (activity == null) {
            ItemPersistence.persistItem(context, newShortcutItem, category);
        } else {
            activity.onShortcutReceived(newShortcutItem);
        }
    }
}
