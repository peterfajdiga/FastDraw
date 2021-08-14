package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class DoubleTap implements View.OnTouchListener {
    private static final int DOUBLE_TAP_TIME = ViewConfiguration.getDoubleTapTimeout();

    private final int pointerCount;
    private final GestureListener listener;
    private long touchDownTime;

    public DoubleTap(final int pointerCount, final GestureListener listener) {
        this.pointerCount = pointerCount;
        this.listener = listener;
        cancel();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getPointerCount() != pointerCount) {
            cancel();
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (touchDownTime != Long.MAX_VALUE && System.currentTimeMillis() - touchDownTime <= DOUBLE_TAP_TIME) {
                    finish();
                    return true;
                } else {
                    start();
                    return false;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                return false;
            default:
                cancel();
                return false;
        }
    }

    private void start() {
        touchDownTime = System.currentTimeMillis();
    }

    private void cancel() {
        touchDownTime = Long.MAX_VALUE;
    }

    private void finish() {
        listener.onGesture();
    }
}
