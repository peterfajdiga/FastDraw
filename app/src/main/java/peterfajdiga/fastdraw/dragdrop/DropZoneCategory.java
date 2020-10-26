package peterfajdiga.fastdraw.dragdrop;

import android.content.Context;
import android.view.View;

public class DropZoneCategory extends DropZone {

    @Override
    protected void onDrop(View view) {
        final String newCategoryName = (String)view.getTag();
        final Owner owner = castToOwner(view.getContext());
        owner.onDraggedItemChangeCategory(newCategoryName);
    }

    @Override
    protected Owner castToOwner(final Context context) {
        if (context instanceof Owner) {
            return (Owner)context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement DropZoneCategory.Owner");
        }
    }

    public interface Owner<T> extends DropZone.Owner<T> {
        void onDraggedItemChangeCategory(String newCategoryName);
    }
}
