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
        final View actionWidgetAddView = dialogView.findViewById(R.id.action_widget_add);
        actionWidgetAddView.setOnClickListener(v -> {
            dialog.cancel();
            activity.showCreateWidgetDialog();
        });

        final View actionWidgetEditView = dialogView.findViewById(R.id.action_widget_edit);
        actionWidgetEditView.setOnClickListener(v -> {
            dialog.cancel();
            activity.editWidget();
        });

        if (activity.hasWidget()) {
            actionWidgetAddView.setVisibility(View.GONE);
            actionWidgetEditView.setVisibility(View.VISIBLE);
        } else {
            actionWidgetAddView.setVisibility(View.VISIBLE);
            actionWidgetEditView.setVisibility(View.GONE);
        }

        final View actionHiddenShow = dialogView.findViewById(R.id.action_hidden_show);
        actionHiddenShow.setOnClickListener(v -> {
            dialog.cancel();
            activity.setHiddenVisibility(true);
        });

        final View actionHiddenHide = dialogView.findViewById(R.id.action_hidden_hide);
        actionHiddenHide.setOnClickListener(v -> {
            dialog.cancel();
            activity.setHiddenVisibility(false);
        });

        // TODO: don't display either when there are no hidden items
        if (activity.isHiddenCategoryVisible()) {
            actionHiddenShow.setVisibility(View.GONE);
            actionHiddenHide.setVisibility(View.VISIBLE);
        } else {
            actionHiddenShow.setVisibility(View.VISIBLE);
            actionHiddenHide.setVisibility(View.GONE);
        }

        final View actionSettingsView = dialogView.findViewById(R.id.action_settings);
        actionSettingsView.setOnClickListener(v -> {
            dialog.cancel();
            activity.openSettings();
        });
    }
}
