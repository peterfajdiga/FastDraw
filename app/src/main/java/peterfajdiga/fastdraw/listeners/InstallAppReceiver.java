package peterfajdiga.fastdraw.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.item.AppItem;

public class InstallAppReceiver extends BroadcastReceiver {

    private MainActivity activity = null;

    public InstallAppReceiver(MainActivity context) {
        super();
        this.activity = context;
    }

    @Override
    public void onReceive(Context context, Intent data) {
        String packageName = data.getData().getEncodedSchemeSpecificPart();
        switch (data.getAction()) {
            case Intent.ACTION_PACKAGE_REMOVED: {
                AppItemManager.removeAppItems(activity.getPager(), packageName);
                break;
            }
            case Intent.ACTION_PACKAGE_CHANGED: {
                AppItemManager.removeAppItems(activity.getPager(), packageName);
                // lack of break is intentional
            }
            case Intent.ACTION_PACKAGE_ADDED: {
                AppItemManager.addAppItems(context, activity.getPager(), packageName);
                break;
            }
            //default: assert false;
        }
    }
}
