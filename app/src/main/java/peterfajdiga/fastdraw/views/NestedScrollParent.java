package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class NestedScrollParent extends NestedScrollView {
    private UnscrolledHeightCalculator scrollChildCalc;
    private OnMeasureListener onMeasureListener;

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

    public void setOnMeasureListener(@Nullable final OnMeasureListener onMeasureListener) {
        this.onMeasureListener = onMeasureListener;
    }

    @Override
    protected void onOverScrolled(final int scrollX, final int scrollY, final boolean clampedX, final boolean clampedY) {
        // prevent superclass from calling `super.scrollTo`
        scrollTo(scrollX, scrollY);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        // prevent scrolling when interacting with widgets // TODO: allow scrolling from category tabs
        return false;
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
        if (!awakenScrollBars()) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (onMeasureListener != null) {
            onMeasureListener.onMeasure();
        }
    }

    @Override
    public int computeVerticalScrollRange() {
        if (scrollChildCalc == null) {
            return 0;
        }
        return scrollChildCalc.getContentHeight();
    }

    @Override
    public int computeVerticalScrollOffset() {
        if (scrollChildCalc == null) {
            return 0;
        }
        return scrollChildCalc.getVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtent() {
        if (scrollChildCalc == null) {
            return 0;
        }
        return scrollChildCalc.getVisibleHeight();
    }

    public interface OnMeasureListener {
        void onMeasure();
    }
}
