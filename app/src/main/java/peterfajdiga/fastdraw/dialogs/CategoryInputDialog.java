package peterfajdiga.fastdraw.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public abstract class CategoryInputDialog extends DialogFragment {
    private static final String INITIAL_CATEGORY_NAME_KEY = "categoryName";
    private static final String TITLE_KEY = "title";
    private static final String POSITIVE_BUTTON_TEXT_KEY = "positiveButtonText";

    public abstract void onPositiveButton(@NonNull String initialCategoryName, @NonNull String inputtedCategoryName);

    @Override
    @NonNull
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        final String initialCategoryName = arguments.getString(INITIAL_CATEGORY_NAME_KEY);
        final String title = arguments.getString(TITLE_KEY);
        final String positiveButtonText = arguments.getString(POSITIVE_BUTTON_TEXT_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        final EditText input = new EditText(getActivity());
        input.setSelectAllOnFocus(true);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setText(initialCategoryName);
        builder.setView(input);

        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onPositiveButton(initialCategoryName, input.getText().toString().toUpperCase());
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        Dialog d = builder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }

    protected void setup(@NonNull final String initialCategoryName, @NonNull final String title, @NonNull final String positiveButtonText) {
        final Bundle args = new Bundle();
        args.putString(INITIAL_CATEGORY_NAME_KEY, initialCategoryName);
        args.putString(TITLE_KEY, title);
        args.putString(POSITIVE_BUTTON_TEXT_KEY, positiveButtonText);
        this.setArguments(args);
    }
}
