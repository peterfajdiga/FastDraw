package peterfajdiga.fastdraw.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;

public class ActionsSheet extends BottomSheetDialogFragment {

    @Override
    public void setupDialog(final Dialog dialog, final int style) {
        final MainActivity activity = (MainActivity)getActivity();
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.sheet_actions, null);
        dialog.setContentView(dialogView);

        final View actionWallpaperView = dialogView.findViewById(R.id.action_wallpaper);
        actionWallpaperView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                activity.openWallpaperPicker();
            }
        });

        final View actionShortcutView = dialogView.findViewById(R.id.action_shortcut);
        actionShortcutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                activity.showCreateShortcutDialog();
            }
        });

        final View actionRenameCategoryView = dialogView.findViewById(R.id.action_rename_category);
        actionRenameCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                activity.renameCurrentCategory();
            }
        });

        final View actionSettingsView = dialogView.findViewById(R.id.action_settings);
        actionSettingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                activity.openSettings();
            }
        });
    }
}
