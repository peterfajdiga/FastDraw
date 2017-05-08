package peterfajdiga.fastdraw.listeners;

import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;

import peterfajdiga.fastdraw.activities.MainActivity;

public abstract class DropZone implements View.OnDragListener {

    protected int hoverColor = 0x40FFFFFF;
    private Drawable defaultBg = null;

    @Override
    public boolean onDrag(View view, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                return ((MainActivity)view.getContext()).draggedItem != null;
            }
            case DragEvent.ACTION_DRAG_ENTERED: {
                defaultBg = view.getBackground();
                view.setBackgroundColor(hoverColor);
                break;
            }
            case DragEvent.ACTION_DRAG_EXITED: {
                view.setBackground(defaultBg);
                break;
            }
            case DragEvent.ACTION_DROP: {
                view.setBackground(defaultBg);
                onDrop(view);
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                defaultBg = null;
                ((MainActivity)view.getContext()).hideDropZones();
                break;
            }
        }
        return true;
    }

    protected abstract void onDrop(View view);
}
