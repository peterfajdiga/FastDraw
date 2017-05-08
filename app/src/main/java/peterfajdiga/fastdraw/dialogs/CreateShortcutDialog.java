package peterfajdiga.fastdraw.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import java.util.List;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.views.ShortcutArrayAdapter;

public class CreateShortcutDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT, null);
        PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> shortcuts = packageManager.queryIntentActivities(intent, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.shortcut);
        builder.setAdapter(new ShortcutArrayAdapter(getActivity(), shortcuts), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            ResolveInfo shortcut = shortcuts.get(i);
            Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            intent.setComponent(new ComponentName(shortcut.activityInfo.packageName, shortcut.activityInfo.name));
            getActivity().startActivityForResult(intent, MainActivity.INSTALL_SHORTCUT_REQUEST);
            }
        });
        return builder.create();
    }
}
