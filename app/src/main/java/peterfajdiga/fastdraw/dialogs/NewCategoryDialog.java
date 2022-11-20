package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;

import androidx.annotation.NonNull;

public class NewCategoryDialog extends CategorySelectionDialog {
    @Override
    public void onCategorySelected(@NonNull final String categoryName) {
        final Activity activity = requireActivity();
        if (activity instanceof Listener) {
            ((Listener)activity).onNewCategoryDialogSuccess(this, categoryName);
        } else {
            throw new RuntimeException(activity.getLocalClassName() + " must implement NewCategoryDialog.Listener");
        }
    }

    public interface Listener {
        void onNewCategoryDialogSuccess(NewCategoryDialog dialog, String newCategoryName);
    }

    public static NewCategoryDialog newInstance(@NonNull final String title) {
        final NewCategoryDialog dialog = new NewCategoryDialog();
        dialog.setup(title);
        return dialog;
    }
}
