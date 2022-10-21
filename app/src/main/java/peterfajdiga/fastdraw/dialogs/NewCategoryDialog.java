package peterfajdiga.fastdraw.dialogs;

import androidx.annotation.NonNull;

public class NewCategoryDialog extends CategorySelectionDialog {
    private final Listener listener;

    public NewCategoryDialog(@NonNull final Listener listener, @NonNull final String title) {
        this.listener = listener;
        setup("", title);
    }

    @Override
    public void onCategorySelected(@NonNull final String initialCategoryName, @NonNull final String inputtedCategoryName) {
        listener.onNewCategoryDialogSuccess(inputtedCategoryName);
    }

    public interface Listener {
        void onNewCategoryDialogSuccess(String newCategoryName);
    }
}
