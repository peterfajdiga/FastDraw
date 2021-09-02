package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NestedScrollParent extends NestedScrollView {
    private RecyclerView scrollChildView;
    private final Rect targetVisibleRectTmp = new Rect();

    public NestedScrollParent(@NonNull final Context context) {
        super(context);
    }

    public NestedScrollParent(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollParent(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollChildView(@Nullable final RecyclerView view) {
        this.scrollChildView = view;
    }

    @Override
    protected void onOverScrolled(final int scrollX, final int scrollY, final boolean clampedX, final boolean clampedY) {
        // prevent superclass from calling `super.scrollTo`
        scrollTo(scrollX, scrollY);
    }

    @Override
    public void scrollTo(final int x, final int y) {
        if (y > getScrollY() && scrollChildView != null && getItemsHeight(scrollChildView) < getVisibleHeight(scrollChildView)) {
            return; // all items visible, don't scroll
        }
        super.scrollTo(x, y);
    }

    @Override
    public void onNestedPreScroll(@NonNull final View target, final int dx, final int dy, @NonNull final int[] consumed, final int type) {
        if (dy > 0) {
            final int y0 = getScrollY();
            scrollBy(dx, dy);
            final int y1 = getScrollY();
            consumed[0] = dx;
            consumed[1] = y1 - y0;
        }
    }

    // bit hacky, but it works
    private static int getItemsHeight(@NonNull final RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return 0;
        }

        final RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) {
            return 0;
        }

        final int itemCount = adapter.getItemCount();
        if (itemCount == 0) {
            return 0;
        }
        final int colCount = getSpanCount(layoutManager);
        final int lineCount = (int)Math.ceil((double)itemCount / (double)colCount);

        final View child = layoutManager.getChildAt(0);
        if (child == null) {
            return 0;
        }
        return lineCount * child.getHeight();
    }

    private static int getSpanCount(@NonNull final RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager)layoutManager).getSpanCount();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return 1;
        } else {
            return 0;
        }
    }

    private int getVisibleHeight(@NonNull final View target) {
        target.getGlobalVisibleRect(targetVisibleRectTmp);
        return targetVisibleRectTmp.height();
    }
}
