package peterfajdiga.fastdraw.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.logic.ShortcutItem;

public class InstallShortcutReceiver extends BroadcastReceiver {

    private MainActivity activity = null;

    public InstallShortcutReceiver(MainActivity context) {
        super();
        this.activity = context;
    }

    @Override
    public void onReceive(Context context, Intent data) {
        ShortcutItem newShortcut = ShortcutItem.shortcutFromIntent(context, data);
        activity.getPager().moveLauncherItem(newShortcut, context.getString(R.string.default_shortcut_category), true);
    }
}
