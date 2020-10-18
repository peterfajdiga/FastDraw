package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;

import androidx.annotation.NonNull;

public class NewCategoryDialog extends CategoryInputDialog {
    public NewCategoryDialog() {}

    public NewCategoryDialog(@NonNull final String title, @NonNull final String positiveButtonText) {
        setup("", title, positiveButtonText);
    }

    @Override
    public void onPositiveButton(@NonNull final String initialCategoryName, @NonNull final String inputtedCategoryName) {
        getOwner().onNewCategoryDialogSuccess(inputtedCategoryName);
    }

    protected Owner getOwner() {
        final Activity activity = getActivity();
        if (activity instanceof Owner) {
            return (Owner)activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement NewCategoryDialog.Owner");
        }
    }

    public interface Owner {
        void onNewCategoryDialogSuccess(String newCategoryName);
    }
}
