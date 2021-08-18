package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class Pinch implements View.OnTouchListener {
    private static final float THRESHOLD_DP = 200f;

    private final float thresholdSq;
    private final float mult;
    private final GestureListener listener;
    private float startDistSq;

    public Pinch(final DisplayMetrics metrics, final boolean unpinch, final GestureListener listener) {
        final float threshold = THRESHOLD_DP * metrics.density;
        this.thresholdSq = threshold * threshold;

        if (unpinch) {
            mult = 1.0f;
        } else {
            mult = -1.0f;
        }

        this.listener = listener;
        cancel();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getPointerCount() != 2) {
            cancel();
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                start(event);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!started()) {
                    start(event);
                }
                return false;
            case MotionEvent.ACTION_POINTER_UP:
                return finishMaybe(event);
            default:
                cancel();
                return false;
        }
    }

    private void start(final MotionEvent event) {
        startDistSq = distSq(event);
    }

    private boolean started() {
        return !Float.isNaN(startDistSq);
    }

    private void cancel() {
        startDistSq = Float.NaN;
    }

    private boolean finishMaybe(final MotionEvent event) {
        final float delta = distSq(event) - startDistSq;

        final boolean finished;
        if (delta * mult >= thresholdSq) {
            listener.onGesture();
            finished = true;
        } else {
            finished = false;
        }

        cancel();
        return finished;
    }

    private static float distSq(final MotionEvent event) {
        assert event.getPointerCount() == 2;
        final float dx = event.getX(0) - event.getX(1);
        final float dy = event.getY(0) - event.getY(1);
        return dx*dx + dy*dy;
    }
}
