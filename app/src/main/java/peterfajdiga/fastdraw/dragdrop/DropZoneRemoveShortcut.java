package peterfajdiga.fastdraw.dragdrop;

import android.content.Context;
import android.view.View;

public class DropZoneRemoveShortcut extends DropZone {

    public DropZoneRemoveShortcut() {
        hoverColor = 0x60f44336;
    }

    @Override
    protected void onDrop(final View view) {
        final Owner owner = castToOwner(view.getContext());
        owner.onDraggedItemRemove();
    }

    @Override
    protected Owner castToOwner(final Context context) {
        if (context instanceof Owner) {
            return (Owner)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DropZoneRemoveShortcut.Owner");
        }
    }


    public interface Owner<T> extends DropZone.Owner<T> {
        void onDraggedItemRemove();
    }
}
