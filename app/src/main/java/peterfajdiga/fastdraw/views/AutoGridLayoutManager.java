package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AutoGridLayoutManager extends GridLayoutManager {
    private final int minSpanWidth;

    public AutoGridLayoutManager(final Context context, final int minSpanWidth, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.minSpanWidth = minSpanWidth;
    }

    public AutoGridLayoutManager(final Context context, final int minSpanWidth) {
        super(context, 1);
        this.minSpanWidth = minSpanWidth;
    }

    public AutoGridLayoutManager(final Context context, final int minSpanWidth, final int orientation, final boolean reverseLayout) {
        super(context, 1, orientation, reverseLayout);
        this.minSpanWidth = minSpanWidth;
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        setSpanCount(Math.max(1, getAvailableWidth() / minSpanWidth));
        super.onLayoutChildren(recycler, state);
    }

    private int getAvailableWidth() {
        if (getOrientation() == VERTICAL) {
            return getWidth() - getPaddingLeft() - getPaddingRight();
        } else {
            return getHeight() - getPaddingTop() - getPaddingBottom();
        }
    }
}
