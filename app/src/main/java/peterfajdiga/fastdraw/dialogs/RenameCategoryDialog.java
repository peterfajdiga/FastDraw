package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;

import androidx.annotation.NonNull;

public class RenameCategoryDialog extends CategorySelectionDialog {
    @Override
    public void onCategorySelected(@NonNull final String categoryName) {
        final Activity activity = requireActivity();
        if (activity instanceof Listener) {
            ((Listener)activity).onRenameCategoryDialogSuccess(this, categoryName);
        } else {
            throw new RuntimeException(activity.getLocalClassName() + " must implement RenameCategoryDialog.Listener");
        }
    }

    public interface Listener {
        void onRenameCategoryDialogSuccess(CategorySelectionDialog dialog, String categoryName);
    }

    public static RenameCategoryDialog newInstance(@NonNull final String title) {
        final RenameCategoryDialog dialog = new RenameCategoryDialog();
        dialog.setup(title);
        return dialog;
    }
}
