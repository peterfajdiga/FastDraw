package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;

import androidx.annotation.NonNull;

public class RenameCategoryDialog extends CategorySelectionDialog {
    @Override
    public void onCategorySelected(@NonNull final String initialCategoryName, @NonNull final String inputtedCategoryName) {
        final Activity activity = requireActivity();
        if (activity instanceof RenameCategoryDialog.Listener) {
            ((RenameCategoryDialog.Listener)activity).onRenameCategoryDialogSuccess(this, initialCategoryName, inputtedCategoryName);
        } else {
            throw new RuntimeException(activity.getLocalClassName() + " must implement RenameCategoryDialog.Listener");
        }
    }

    public interface Listener {
        void onRenameCategoryDialogSuccess(CategorySelectionDialog dialog, String oldCategoryName, String newCategoryName);
    }

    public static RenameCategoryDialog newInstance(@NonNull final String initialCategoryName, @NonNull final String title) {
        final RenameCategoryDialog dialog = new RenameCategoryDialog();
        dialog.setup(initialCategoryName, title);
        return dialog;
    }
}
