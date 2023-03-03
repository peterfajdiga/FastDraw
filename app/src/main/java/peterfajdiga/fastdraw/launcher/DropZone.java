package peterfajdiga.fastdraw.launcher;

import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;

import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;

public class DropZone implements View.OnDragListener {
    private final Listener listener;
    private final int hoverColor;
    private Drawable defaultBg = null;

    public DropZone(final Listener listener, final boolean red) {
        this.listener = listener;
        this.hoverColor = red ? 0x60f44336 : 0x40FFFFFF;
    }

    @Override
    public boolean onDrag(final View view, final DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                return event.getLocalState() instanceof LauncherItem;
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
                listener.onDrop((LauncherItem)event.getLocalState());
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                defaultBg = null;
                break;
            }
        }
        return true;
    }

    public interface Listener {
        void onDrop(LauncherItem draggedItem);
    }
}
