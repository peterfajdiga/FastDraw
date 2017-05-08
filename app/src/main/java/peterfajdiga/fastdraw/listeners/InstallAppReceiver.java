package peterfajdiga.fastdraw.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.logic.AppItem;

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
                activity.getPagerAdapter().removeAppItems(packageName);
                break;
            }
            case Intent.ACTION_PACKAGE_CHANGED: {
                activity.getPagerAdapter().removeAppItems(packageName);
                // lack of break is intentional
            }
            case Intent.ACTION_PACKAGE_ADDED: {
                final PackageManager packageManager = context.getPackageManager();
                Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
                launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                launcherIntent.setPackage(packageName);
                for (ResolveInfo resInfo : packageManager.queryIntentActivities(launcherIntent, 0)) {
                    AppItem appItem = new AppItem(
                        packageName,
                        resInfo.activityInfo.name,
                        resInfo.loadLabel(packageManager).toString(),
                        resInfo.activityInfo.loadIcon(packageManager)
                    );
                    appItem.setCategoryNoDirty(context.getString(R.string.default_category), activity);
                }
                break;
            }
            //default: assert false;
        }
    }
}
