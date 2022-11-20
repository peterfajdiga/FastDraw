package peterfajdiga.fastdraw.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.function.Consumer;

public class DialogUtils {
    private DialogUtils() {}

    @NonNull
    public static Bundle getOrCreateArguments(final DialogFragment dialog) {
        final Bundle existingArgs = dialog.getArguments();
        if (existingArgs != null) {
            return existingArgs;
        } else {
            return new Bundle();
        }
    }

    public static void modifyArguments(final DialogFragment dialog, final Consumer<Bundle> f) {
        final Bundle args = getOrCreateArguments(dialog);
        f.accept(args);
        dialog.setArguments(args);
    }
}
