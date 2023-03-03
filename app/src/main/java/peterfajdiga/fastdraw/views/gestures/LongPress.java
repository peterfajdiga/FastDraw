package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class LongPress implements View.OnTouchListener {
    private static final int LONG_PRESS_TIME = ViewConfiguration.getLongPressTimeout();
    private static final float MOVE_TOLERANCE_DP = 16f;

    private final float moveToleranceSq;
    private final GestureListener listener;
    private long touchDownTime;
    private final PointF touchDownPoint = new PointF();

    public LongPress(final DisplayMetrics metrics, final GestureListener listener) {
        final float moveTolerance = MOVE_TOLERANCE_DP * metrics.density;
        this.moveToleranceSq = moveTolerance * moveTolerance;
        this.listener = listener;
        cancel();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getPointerCount() != 1) {
            cancel();
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                start(event);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (distSq(touchDownPoint, event.getX(), event.getY()) > moveToleranceSq) {
                    cancel();
                }
                return finishMaybe(v);
            default:
                cancel();
                return false;
        }
    }

    private void start(final MotionEvent event) {
        touchDownTime = System.currentTimeMillis();
        touchDownPoint.set(event.getX(), event.getY());
    }

    private void cancel() {
        touchDownTime = Long.MAX_VALUE;
    }

    private boolean finishMaybe(final View v) {
        if (System.currentTimeMillis() - touchDownTime >= LONG_PRESS_TIME) {
            cancel();
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            listener.onGesture();
            return true;
        }
        return false;
    }

    private float distSq(final PointF p0, final float x1, final float y1) {
        final float dx = x1 - p0.x;
        final float dy = y1 - p0.y;
        return dx*dx + dy*dy;
    }
}
