package peterfajdiga.fastdraw.listeners;

import android.view.View;

import peterfajdiga.fastdraw.activities.MainActivity;

public class DropZoneCategory extends DropZone {

    @Override
    protected void onDrop(View view) {
        MainActivity activity = ((MainActivity)view.getContext());
        String categoryName = (String)view.getTag();
        activity.draggedItem.setCategory(categoryName, activity, true);
    }
}
