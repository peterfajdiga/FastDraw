package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.views.ShortcutArrayAdapter;
import peterfajdiga.fastdraw.views.ShortcutsAdapter;

public class CreateShortcutDialog extends BottomSheetDialogFragment implements ShortcutsAdapter.OnShortcutClickedListener {

//    @Override
//    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
//        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT, null);
//        PackageManager packageManager = getActivity().getPackageManager();
//        final List<ResolveInfo> shortcuts = packageManager.queryIntentActivities(intent, 0);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.shortcut);
//        builder.setAdapter(new ShortcutArrayAdapter(getActivity(), shortcuts), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
    //            ResolveInfo shortcut = shortcuts.get(i);
    //            Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
    //            intent.setComponent(new ComponentName(shortcut.activityInfo.packageName, shortcut.activityInfo.name));
    //            getActivity().startActivityForResult(intent, MainActivity.INSTALL_SHORTCUT_REQUEST);
//            }
//        });
//        return builder.create();
//    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        final Activity activity = getActivity();
        final Resources res = activity.getResources();
        final PackageManager packageManager = activity.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT, null);
        final List<ResolveInfo> shortcuts = packageManager.queryIntentActivities(intent, 0);

        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet, null);
        dialog.setContentView(dialogView);

        final TextView header = (TextView)dialogView.findViewById(R.id.header);
        header.setText(res.getString(R.string.shortcut));
        header.setVisibility(View.VISIBLE);

        final RecyclerView container = (RecyclerView)dialogView.findViewById(R.id.container);
        final ShortcutsAdapter shortcutsAdapter = new ShortcutsAdapter(shortcuts);
        shortcutsAdapter.setOnShortcutClickedListener(this);
        container.setAdapter(shortcutsAdapter);
        container.setLayoutManager(new GridLayoutManager(activity, 4));
    }

    @Override
    public void OnShortcutClicked(ResolveInfo shortcut) {
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        intent.setComponent(new ComponentName(shortcut.activityInfo.packageName, shortcut.activityInfo.name));
        getActivity().startActivityForResult(intent, MainActivity.INSTALL_SHORTCUT_REQUEST);
        getDialog().cancel();
    }

    @Override
    public void OnShortcutLongClicked(ResolveInfo shortcut) {
        AppItemManager.showPackageDetails(getContext(), shortcut.activityInfo.packageName);
    }
}
