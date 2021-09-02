package peterfajdiga.fastdraw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InstallAppReceiver extends BroadcastReceiver {
    private final Owner owner;

    public InstallAppReceiver(final Owner context) {
        super();
        this.owner = context;
    }

    @Override
    public void onReceive(Context context, Intent data) {
        String packageName = data.getData().getEncodedSchemeSpecificPart();
        switch (data.getAction()) {
            case Intent.ACTION_PACKAGE_REMOVED: {
                Log.d("InstallAppReceiver", "removed package " + packageName);
                owner.onAppRemove(packageName);
                break;
            }
            case Intent.ACTION_PACKAGE_CHANGED: {
                Log.d("InstallAppReceiver", "changed package " + packageName);
                owner.onAppChange(packageName);
                break;
            }
            case Intent.ACTION_PACKAGE_ADDED: {
                Log.d("InstallAppReceiver", "added package " + packageName);
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
