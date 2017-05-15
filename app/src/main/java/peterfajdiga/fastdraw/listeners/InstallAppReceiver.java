package peterfajdiga.fastdraw.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
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
                activity.getPager().removeAppItems(packageName);
                break;
            }
            case Intent.ACTION_PACKAGE_CHANGED: {
                activity.getPager().removeAppItems(packageName);
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
                    String categoryName = context.getString(R.string.default_category);
                    appItem.setCategoryNoDirty(categoryName);
                    activity.getPager().addLauncherItem(appItem);
                }
                break;
            }
            //default: assert false;
        }
    }
}
