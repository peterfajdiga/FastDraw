package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class NestedScrollParent extends NestedScrollView {
    private UnscrolledHeightCalculator scrollChildCalc;

    public NestedScrollParent(@NonNull final Context context) {
        super(context);
    }

    public NestedScrollParent(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollParent(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollChildCalc(@Nullable final UnscrolledHeightCalculator scrollChildCalc) {
        this.scrollChildCalc = scrollChildCalc;
    }

    @Override
    protected void onOverScrolled(final int scrollX, final int scrollY, final boolean clampedX, final boolean clampedY) {
        // prevent superclass from calling `super.scrollTo`
        scrollTo(scrollX, scrollY);
    }

    @Override
    public void scrollTo(final int x, final int y) {
        final int dy = y - getScrollY();
        if (dy > 0 && scrollChildCalc != null) {
            // prevent scrolling more than necessary to see all useful content in scrollChildView
            final int maxScrollY = scrollChildCalc.getUnscrolledHeight();
            final int newScrollY = getScrollY() + Math.min(dy, maxScrollY);
            super.scrollTo(x, newScrollY);
        } else {
            super.scrollTo(x, y);
        }
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
}
