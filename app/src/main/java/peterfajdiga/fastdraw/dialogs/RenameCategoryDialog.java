package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;

import androidx.annotation.NonNull;

public class RenameCategoryDialog extends CategorySelectionDialog {
    private static final String INITIAL_CATEGORY_NAME_KEY = "categoryName";

    @Override
    public void onCategorySelected(@NonNull final String categoryName) {
        final Activity activity = requireActivity();
        if (activity instanceof RenameCategoryDialog.Listener) {
            ((RenameCategoryDialog.Listener)activity).onRenameCategoryDialogSuccess(
                this,
                this.requireArguments().getString(INITIAL_CATEGORY_NAME_KEY),
                categoryName
            );
        } else {
            throw new RuntimeException(activity.getLocalClassName() + " must implement RenameCategoryDialog.Listener");
        }
    }

    public interface Listener {
        void onRenameCategoryDialogSuccess(CategorySelectionDialog dialog, String oldCategoryName, String newCategoryName);
    }

    public static RenameCategoryDialog newInstance(@NonNull final String initialCategoryName, @NonNull final String title) {
        final RenameCategoryDialog dialog = new RenameCategoryDialog();
        dialog.setup(title);
        DialogUtils.modifyArguments(dialog, args -> args.putString(INITIAL_CATEGORY_NAME_KEY, initialCategoryName));
        return dialog;
    }
}
