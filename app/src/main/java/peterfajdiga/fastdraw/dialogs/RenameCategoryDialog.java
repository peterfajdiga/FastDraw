package peterfajdiga.fastdraw.dialogs;

import androidx.annotation.NonNull;

public class RenameCategoryDialog extends CategorySelectionDialog {
    private final RenameCategoryDialog.Listener listener;

    public RenameCategoryDialog(@NonNull final RenameCategoryDialog.Listener listener, @NonNull final String initialCategoryName, @NonNull final String title) {
        this.listener = listener;
        setup(initialCategoryName, title);
    }

    @Override
    public void onCategorySelected(@NonNull final String initialCategoryName, @NonNull final String inputtedCategoryName) {
        listener.onRenameCategoryDialogSuccess(initialCategoryName, inputtedCategoryName);
    }

    public interface Listener {
        void onRenameCategoryDialogSuccess(String oldCategoryName, String newCategoryName);
    }
}
