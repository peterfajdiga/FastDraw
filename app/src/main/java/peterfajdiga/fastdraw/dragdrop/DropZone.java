package peterfajdiga.fastdraw.dragdrop;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;

abstract class DropZone implements View.OnDragListener {

    protected int hoverColor = 0x40FFFFFF;
    private Drawable defaultBg = null;

    @Override
    public boolean onDrag(final View view, final DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                return castToOwner(view.getContext()).getDraggedItem() != null;
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
                castToOwner(view.getContext()).onDragEnded();
                break;
            }
        }
        return true;
    }

    protected abstract void onDrop(View view);


    protected Owner castToOwner(final Context context) {
        if (context instanceof Owner) {
            return (Owner)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DropZone.Owner");
        }
    }


    public interface Owner<T> {
        void onDragEnded();
        T getDraggedItem();
    }
}
