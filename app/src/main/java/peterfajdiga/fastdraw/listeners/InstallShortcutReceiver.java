package peterfajdiga.fastdraw.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class InstallShortcutReceiver extends BroadcastReceiver {

    private Owner owner = null;

    public InstallShortcutReceiver() {
        super();
    }

    public InstallShortcutReceiver(Owner context) {
        super();
        this.owner = context;
    }

    @Override
    public void onReceive(Context context, Intent data) {
        ShortcutItem newShortcutItem = ShortcutItem.shortcutFromIntent(context, data);
        newShortcutItem.setCategory(context.getString(R.string.default_shortcut_category));
        if (owner == null) {
            newShortcutItem.persist(context);
        } else {
            owner.onShortcutReceived(newShortcutItem);
        }
    }


    public interface Owner {
        void onShortcutReceived(ShortcutItem newShortcut);
    }
}
