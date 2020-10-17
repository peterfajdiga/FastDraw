package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import peterfajdiga.fastdraw.R;

public class RenameCategoryDialog extends CategoryInputDialog {
    public RenameCategoryDialog() {}

    public RenameCategoryDialog(@NonNull final String initialCategoryName, @NonNull final String title, @NonNull final String positiveButtonText) {
        setup(initialCategoryName, title, positiveButtonText);
    }

    @Override
    public void onPositiveButton(@NonNull final String initialCategoryName, @NonNull final String inputtedCategoryName) {
        getOwner().onRenameCategoryDialogSuccess(initialCategoryName, inputtedCategoryName);
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
        void onRenameCategoryDialogSuccess(String oldCategoryName, String newCategoryName);
    }
}
