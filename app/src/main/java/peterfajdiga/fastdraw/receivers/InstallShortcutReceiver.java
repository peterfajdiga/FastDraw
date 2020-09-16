package peterfajdiga.fastdraw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class InstallShortcutReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent data) {
        ShortcutItem newShortcutItem = ShortcutItem.shortcutFromIntent(context, data);
        newShortcutItem.setCategory(context.getString(R.string.default_shortcut_category));
        final MainActivity activity = MainActivity.getInstance();
        if (activity == null) {
            newShortcutItem.persist(context);
        } else {
            activity.onShortcutReceived(newShortcutItem);
        }
    }
}
