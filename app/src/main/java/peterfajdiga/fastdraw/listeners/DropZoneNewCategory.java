package peterfajdiga.fastdraw.listeners;

import android.content.Context;
import android.view.View;

public class DropZoneNewCategory extends DropZone {

    @Override
    protected void onDrop(View view) {
        final Owner owner = castToOwner(view.getContext());
        owner.onDraggedItemNewCategory();
    }


    @Override
    protected Owner castToOwner(final Context context) {
        if (context instanceof Owner) {
            return (Owner)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DropZoneNewCategory.Owner");
        }
    }


    public interface Owner<T> extends DropZone.Owner<T> {
        void onDraggedItemNewCategory();
    }
}
