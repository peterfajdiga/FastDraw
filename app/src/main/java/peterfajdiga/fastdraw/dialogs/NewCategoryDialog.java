package peterfajdiga.fastdraw.dialogs;

import androidx.annotation.NonNull;

public class NewCategoryDialog extends CategoryInputDialog {
    private final Listener listener;

    public NewCategoryDialog(@NonNull final Listener listener, @NonNull final String title, @NonNull final String positiveButtonText) {
        this.listener = listener;
        setup("", title, positiveButtonText);
    }

    @Override
    public void onPositiveButton(@NonNull final String initialCategoryName, @NonNull final String inputtedCategoryName) {
        listener.onNewCategoryDialogSuccess(inputtedCategoryName);
    }

    public interface Listener {
        void onNewCategoryDialogSuccess(String newCategoryName);
    }
}
