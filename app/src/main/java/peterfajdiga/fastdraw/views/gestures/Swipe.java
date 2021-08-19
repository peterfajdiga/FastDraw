package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class Swipe implements View.OnTouchListener {
    private static final float THRESHOLD_DP = 60f;

    private final float threshold;
    private final Direction direction;
    private final int pointerCount;
    private final GestureListener listener;

    private boolean started;
    private final PointF touchDownCenter = new PointF();
    private final PointF touchUpCenter = new PointF();
    private final MotionEvent.PointerCoords pointerCoordsTmp = new MotionEvent.PointerCoords();

    public Swipe(final DisplayMetrics metrics, final Direction direction, final int pointerCount, final GestureListener listener) {
        this.threshold = THRESHOLD_DP * metrics.density;
        this.direction = direction;
        this.pointerCount = pointerCount;
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getPointerCount() != pointerCount) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                start(event);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!started) {
                    start(event);
                }
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                return maybeFinish(event);
            default:
                cancel();
                return false;
        }
    }

    private void start(final MotionEvent event) {
        getCenter(event, touchDownCenter);
        started = true;
    }

    private void cancel() {
        started = false;
    }

    private boolean maybeFinish(final MotionEvent event) {
        getCenter(event, touchUpCenter);
        final boolean finished = started && (
            direction == Direction.UP    && touchUpCenter.y - touchDownCenter.y < -threshold ||
            direction == Direction.DOWN  && touchUpCenter.y - touchDownCenter.y >  threshold ||
            direction == Direction.LEFT  && touchUpCenter.x - touchDownCenter.x < -threshold ||
            direction == Direction.RIGHT && touchUpCenter.x - touchDownCenter.x >  threshold
        );
        if (finished) {
            listener.onGesture();
        }
        cancel();
        return finished;
    }

    private void getCenter(final MotionEvent event, final PointF outPoint) {
        final int n = event.getPointerCount();
        outPoint.set(0.0f, 0.0f);
        for (int i = 0; i < n; i++) {
            event.getPointerCoords(i, pointerCoordsTmp);
            outPoint.x += pointerCoordsTmp.x;
            outPoint.y += pointerCoordsTmp.y;
        }
        outPoint.x /= n;
        outPoint.y /= n;
    }

    public enum Direction {UP, DOWN, LEFT, RIGHT}
}
