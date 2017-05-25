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

    private Owner owner = null;

    public InstallAppReceiver(Owner context) {
        super();
        this.owner = context;
    }

    @Override
    public void onReceive(Context context, Intent data) {
        String packageName = data.getData().getEncodedSchemeSpecificPart();
        switch (data.getAction()) {
            case Intent.ACTION_PACKAGE_REMOVED: {
                owner.onAppRemove(packageName);
                break;
            }
            case Intent.ACTION_PACKAGE_CHANGED: {
                owner.onAppChange(packageName);
                break;
            }
            case Intent.ACTION_PACKAGE_ADDED: {
                owner.onAppInstall(packageName);
                break;
            }
            //default: assert false;
        }
    }


    public interface Owner {
        void onAppInstall(String packageName);
        void onAppChange(String packageName);
        void onAppRemove(String packageName);
    }
}
