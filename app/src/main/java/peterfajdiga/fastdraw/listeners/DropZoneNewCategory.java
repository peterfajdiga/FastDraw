package peterfajdiga.fastdraw.listeners;

import android.view.View;

import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.dialogs.NewCategoryDialog;

public class DropZoneNewCategory extends DropZone {

    @Override
    protected void onDrop(View view) {
        MainActivity activity = ((MainActivity)view.getContext());
        NewCategoryDialog dialog = new NewCategoryDialog();
        dialog.show(activity.getFragmentManager(), "NewCategoryDialog");
    }
}
