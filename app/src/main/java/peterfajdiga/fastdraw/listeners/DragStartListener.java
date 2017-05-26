package peterfajdiga.fastdraw.listeners;

import android.content.Context;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;

public class DragStartListener implements AdapterView.OnItemLongClickListener {

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        // start drag
        final View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
        if (Build.VERSION.SDK_INT < 24) {
            view.startDrag(null, shadow, null, 0);
        } else {
            view.startDragAndDrop(null, shadow, null, 0);
        }

        final Owner owner = castToOwner(view.getContext());
        owner.onDragStarted(adapterView.getItemAtPosition(pos));

        return false;
    }


    protected Owner castToOwner(final Context context) {
        if (context instanceof Owner) {
            return (Owner)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DragStartListener.Owner");
        }
    }


    public interface Owner<T> {
        void onDragStarted(T draggedItem);
    }
}
