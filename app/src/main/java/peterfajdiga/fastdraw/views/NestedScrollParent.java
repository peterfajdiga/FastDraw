package peterfajdiga.fastdraw.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class NestedScrollParent extends NestedScrollView {
    private NestedScrollChildManager scrollChildManager;
    private OnMeasureListener onMeasureListener;
    private final OverScrollUpController overScrollUpController = new OverScrollUpController();

    public NestedScrollParent(@NonNull final Context context) {
        super(context);
    }

    public NestedScrollParent(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollParent(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollChildManager(@Nullable final NestedScrollChildManager scrollChild) {
        this.scrollChildManager = scrollChild;
    }

    public void setOnMeasureListener(@Nullable final OnMeasureListener onMeasureListener) {
        this.onMeasureListener = onMeasureListener;
    }

    public void setOnOverScrollUpListener(@Nullable final OnOverScrollUpListener onOverScrollUpListener) {
        this.overScrollUpController.setListener(onOverScrollUpListener);
    }

    @Override
    protected void onOverScrolled(final int scrollX, final int scrollY, final boolean clampedX, final boolean clampedY) {
        if (scrollY == 0) {
            overScrollUpController.overScroll();
        } else {
            overScrollUpController.cancel();
        }

        scrollTo(scrollX, scrollY); // prevent superclass from calling `super.scrollTo`
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getScrollY() == 0) {
                    overScrollUpController.press();
                }
                break;
            case MotionEvent.ACTION_UP:
                overScrollUpController.release();
                break;
        }

        return false; // don't call super to prevent scrolling when interacting with widgets // TODO: allow scrolling from category tabs
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            cancelScroll();
        }
        return super.onTouchEvent(ev);
    }

    public void cancelScroll() {
        overScrollUpController.release();
    }

    @Override
    public void scrollTo(final int x, final int y) {
        final int dy = y - getScrollY();
        if (dy > 0 && scrollChildManager != null) {
            // prevent scrolling more than necessary to see all useful content in scrollChildView
            final int maxScrollY = scrollChildManager.getUnscrolledHeight();
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

            overScrollUpController.cancel();
        } else if (dy < 0) {
            overScrollUpController.overScroll();
        }

        // update scroll bar
        if (!awakenScrollBars()) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (onMeasureListener != null) {
            onMeasureListener.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public int computeVerticalScrollRange() {
        if (scrollChildManager == null) {
            return 0;
        }
        return scrollChildManager.getContentHeight();
    }

    @Override
    public int computeVerticalScrollOffset() {
        if (scrollChildManager == null) {
            return 0;
        }
        return scrollChildManager.getVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtent() {
        if (scrollChildManager == null) {
            return 0;
        }

        final int extent = scrollChildManager.getVisibleHeight();
        if (getScrollY() > 0) {
            final int range = computeVerticalScrollRange();
            if (extent >= range) {
                return range - 1; // prevent hiding the scroll bar when all the child's content has been scrolled into view
            }
        }
        return extent;
    }

    public interface OnMeasureListener {
        void onMeasure(int widthMeasureSpec, int heightMeasureSpec);
    }

    public interface OnOverScrollUpListener {
        void onOverScrollUp();
    }

    private static class OverScrollUpController {
        private OnOverScrollUpListener listener;

        public void setListener(final OnOverScrollUpListener listener) {
            this.listener = listener;
        }

        private enum State {
            INACTIVE,
            PRESSED,
            OVER_SCROLLED,
        }

        private State state = State.INACTIVE;

        void cancel() {
            state = State.INACTIVE;
        }

        void press() {
            if (state == State.INACTIVE) {
                state = State.PRESSED;
            }
        }

        void overScroll() {
            if (state == State.PRESSED) {
                state = State.OVER_SCROLLED;
            }
        }

        void release() {
            if (state == State.OVER_SCROLLED && listener != null) {
                listener.onOverScrollUp();
            }
            cancel();
        }
    }
}
