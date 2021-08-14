package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class LongPress implements View.OnTouchListener {
    private static final int LONG_PRESS_TIME = ViewConfiguration.getLongPressTimeout();

    private final int pointerCount;
    private final GestureListener listener;
    private long touchDownTime;

    public LongPress(final int pointerCount, final GestureListener listener) {
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
                start();
                return false;
            case MotionEvent.ACTION_MOVE:
                return finishMaybe(v);
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

    private boolean finishMaybe(final View v) {
        if (System.currentTimeMillis() - touchDownTime >= LONG_PRESS_TIME) {
            cancel();
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            listener.onGesture();
            return true;
        }
        return false;
    }
}
