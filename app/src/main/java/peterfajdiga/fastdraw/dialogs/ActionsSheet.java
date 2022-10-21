package peterfajdiga.fastdraw.dialogs;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;

public class ActionsSheet extends BottomSheetDialogFragment {

    @Override
    public void setupDialog(final Dialog dialog, final int style) {
        final MainActivity activity = (MainActivity)getActivity();
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.sheet_actions, null);
        dialog.setContentView(dialogView);

        final View actionWallpaperView = dialogView.findViewById(R.id.action_wallpaper);
        actionWallpaperView.setOnClickListener(v -> {
            dialog.cancel();
            activity.openWallpaperPicker();
        });

        final View actionShortcutView = dialogView.findViewById(R.id.action_shortcut);
        actionShortcutView.setOnClickListener(v -> {
            dialog.cancel();
            activity.showCreateShortcutDialog();
        });

        // TODO: only display the Remove widget button if there is a widget to remove
        final View actionWidgetView = dialogView.findViewById(R.id.action_widget);
        actionWidgetView.setOnClickListener(v -> {
            dialog.cancel();
            activity.showCreateWidgetDialog();
        });

        final View actionRemoveWidgetView = dialogView.findViewById(R.id.action_remove_widget);
        actionRemoveWidgetView.setOnClickListener(v -> {
            dialog.cancel();
            activity.removeWidget();
        });

        final View actionSettingsView = dialogView.findViewById(R.id.action_settings);
        actionSettingsView.setOnClickListener(v -> {
            dialog.cancel();
            activity.openSettings();
        });
    }
}
