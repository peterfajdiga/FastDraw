package peterfajdiga.fastdraw.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;

public class ActionsSheet extends BottomSheetDialogFragment {
    private Context originalContext;
    private ContextThemeWrapper themedContext;

    @Nullable
    @Override
    public Context getContext() {
        final Context originalContext = super.getContext();
        if (originalContext == null) {
            this.originalContext = null;
            this.themedContext = null;
            return null;
        }

        if (originalContext != this.originalContext) {
            this.originalContext = originalContext;
            this.themedContext = new ContextThemeWrapper(originalContext, R.style.AppTheme_DayNight);
        }
        return this.themedContext;
    }

    @Override
    public View onCreateView(
        @NonNull final LayoutInflater inflater,
        @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState
    ) {
        final View dialogView = inflater.inflate(R.layout.sheet_actions, null);
        final MainActivity activity = (MainActivity)getActivity();

        final View actionWallpaperView = dialogView.findViewById(R.id.action_wallpaper);
        actionWallpaperView.setOnClickListener(v -> {
            getDialog().cancel();
            activity.openWallpaperPicker();
        });

        final View actionShortcutView = dialogView.findViewById(R.id.action_shortcut);
        actionShortcutView.setOnClickListener(v -> {
            getDialog().cancel();
            activity.showCreateShortcutDialog();
        });

        // TODO: only display the Remove widget button if there is a widget to remove
        final View actionWidgetAddView = dialogView.findViewById(R.id.action_widget_add);
        actionWidgetAddView.setOnClickListener(v -> {
            getDialog().cancel();
            activity.showCreateWidgetDialog();
        });

        final View actionWidgetEditView = dialogView.findViewById(R.id.action_widget_edit);
        actionWidgetEditView.setOnClickListener(v -> {
            getDialog().cancel();
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
            getDialog().cancel();
            activity.setHiddenVisibility(true);
        });

        final View actionHiddenHide = dialogView.findViewById(R.id.action_hidden_hide);
        actionHiddenHide.setOnClickListener(v -> {
            getDialog().cancel();
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
            getDialog().cancel();
            activity.openSettings();
        });

        return dialogView;
    }
}
