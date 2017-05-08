package peterfajdiga.fastdraw.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;

public class RenameCategoryDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String categoryName = getArguments().getString("categoryName");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.rename_category);
        final EditText input = new EditText(getActivity());
        input.setSelectAllOnFocus(true);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setText(categoryName);
        builder.setView(input);

        builder.setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MainActivity)getActivity()).renameCategory(categoryName, input.getText().toString().toUpperCase());
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        Dialog d = builder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }
}
