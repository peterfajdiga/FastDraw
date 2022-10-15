package peterfajdiga.fastdraw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
