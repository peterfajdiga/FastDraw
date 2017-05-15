package peterfajdiga.fastdraw.listeners;

import android.view.View;

import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.item.AppItem;

public class DropZoneAppInfo extends DropZone {

    @Override
    protected void onDrop(View view) {
        MainActivity activity = ((MainActivity)view.getContext());
        //assert activity.draggedItem instanceof AppItem;
        AppItem app = (AppItem)activity.draggedItem;
        app.openAppDetails(activity);
    }
}
