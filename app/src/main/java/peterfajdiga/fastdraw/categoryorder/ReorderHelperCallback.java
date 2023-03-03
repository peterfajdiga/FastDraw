package peterfajdiga.fastdraw.categoryorder;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ReorderHelperCallback extends ItemTouchHelper.Callback {

    private final ReorderHelperListener listener;

    public ReorderHelperCallback(ReorderHelperListener listener) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder source, final RecyclerView.ViewHolder target) {
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        // Notify the adapter of the move
        listener.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int i) {}

    // faster, scrolling while dragging
    private final static float SPEED_MULT = 2;
    private final static int MAX_SPEED = 10;

    @Override
    public int interpolateOutOfBoundsScroll(
        RecyclerView recyclerView,
        int viewSize,
        int viewSizeOutOfBounds,
        int totalSize,
        long msSinceStartScroll
    ) {
        final int direction = (int)Math.signum(viewSizeOutOfBounds);
        int speed = (int)Math.round(Math.sqrt(Math.abs(viewSizeOutOfBounds * SPEED_MULT)));
        //int speed = Math.round(viewSizeOutOfBounds * SPEED_MULT);
        //speed *= speed;
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }
        return speed * direction;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }
}
