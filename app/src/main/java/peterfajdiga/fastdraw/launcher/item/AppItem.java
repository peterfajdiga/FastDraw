package peterfajdiga.fastdraw.launcher.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

public class AppItem extends LauncherItem {

    public String packageName;
    private String activityName;

    public AppItem(String packageName, String activityName, String name, Drawable icon) {
        this.packageName  = packageName;
        this.activityName = activityName;
        this.name         = name;
        this.icon         = icon;
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public String getID() {
        return packageName + "\0" + activityName;
    }

    @Override
    public void persist(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("categories", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(getID(), category);
        prefsEditor.apply();
    }

    public void openAppDetails(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }
}
