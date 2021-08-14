package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

public class Pinch implements View.OnTouchListener {
    private static final float THRESHOLD = 500000;

    private final float mult;
    private final GestureListener listener;
    private float startDistSq;

    public Pinch(final boolean unpinch, final GestureListener listener) {
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
                return finishMaybe(event);
            default:
                cancel();
                return false;
        }
    }

    private void start(final MotionEvent event) {
        startDistSq = distSq(event);
    }

    private void cancel() {
        startDistSq = Float.NaN;
    }

    private boolean finishMaybe(final MotionEvent event) {
        final float delta = distSq(event) - startDistSq;
        if (delta * mult >= THRESHOLD) {
            cancel();
            listener.onGesture();
            return true;
        } else {
            return false;
        }
    }

    private static float distSq(final MotionEvent event) {
        assert event.getPointerCount() == 2;
        final float dx = event.getX(0) - event.getX(1);
        final float dy = event.getY(0) - event.getY(1);
        return dx*dx + dy*dy;
    }
}
