package peterfajdiga.fastdraw.listeners;

import android.view.View;

import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;

public class DropZoneRemoveShortcut extends DropZone {

    public DropZoneRemoveShortcut() {
        hoverColor = 0x60f44336;
    }

    @Override
    protected void onDrop(View view) {
        MainActivity activity = ((MainActivity)view.getContext());
        //assert activity.draggedItem instanceof ShortcutItem;
        ShortcutItem item = (ShortcutItem)activity.draggedItem;
        item.remove(activity);
    }
}
