package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.GridView;

import androidx.annotation.NonNull;

class GesturesGridView extends GridView {
    private static final int LONG_CLICK_TIME = ViewConfiguration.getLongPressTimeout();
    private static final int DOUBLE_CLICK_TIME = ViewConfiguration.getDoubleTapTimeout();
    private static final float LONG_CLICK_MOUSE_MOVE_TOLERANCE = 50; // TODO: separate for DOUBLE_CLICK
    private static final float PINCH_DISTANCE_TRIGGER_DELTA = 500000;

    protected long interceptTouchTime;
    protected float interceptTouchX;
    protected float interceptTouchY;

    protected float pinchPrevDistance = 0.0f;
    protected float pinchStartDistance = 0.0f;
    protected float unpinchStartDistance = Float.MAX_VALUE;

    private Listener listener;

    public GesturesGridView(final Context context) {
        super(context);
    }

    public GesturesGridView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public GesturesGridView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GesturesGridView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setListener(@NonNull final Listener listener) {
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // perform pinch on liftoff
            if (pinchStartDistance - pinchPrevDistance > PINCH_DISTANCE_TRIGGER_DELTA) {
                listener.onPinch();
            } else if (pinchPrevDistance - unpinchStartDistance > PINCH_DISTANCE_TRIGGER_DELTA) {
                listener.onUnpinch();
            }
            // reset pinch and unpinch
            pinchPrevDistance = 0.0f;
            pinchStartDistance = 0.0f;
            unpinchStartDistance = Float.MAX_VALUE;
        }

        switch (event.getPointerCount()) {
            case 2: {
                interceptTouchTime = Long.MAX_VALUE; // disable long click
                final float newDistance = distanceSquared(event);

                // pinch detection
                if (pinchStartDistance == 0.0f) {
                    // start pinch
                    pinchStartDistance = newDistance;
                } else if (newDistance > pinchPrevDistance) {
                    // user unpinched, reset pinch
                    pinchStartDistance = 0.0f;
                }

                // unpinch detection
                if (unpinchStartDistance == Float.MAX_VALUE) {
                    // start pinch
                    unpinchStartDistance = newDistance;
                } else if (newDistance < pinchPrevDistance) {
                    // user pinched, reset unpinch
                    unpinchStartDistance = Float.MAX_VALUE;
                }

                // update pinchPrevDistance
                pinchPrevDistance = newDistance;
                break;
            }
            case 1: {
                // long click detection
                if (System.currentTimeMillis() - interceptTouchTime >= LONG_CLICK_TIME) {
                    this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    listener.onLongpress();
                    interceptTouchTime = Long.MAX_VALUE;
                } else if (hasPointerMoved(event.getX(), event.getY())) {
                    interceptTouchTime = Long.MAX_VALUE;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            // get new values
            final long newInterceptTouchTime = System.currentTimeMillis();
            final float newX = event.getX();
            final float newY = event.getY();

            // double click detection
            final long timeSinceLastClick = newInterceptTouchTime - interceptTouchTime;
            if (timeSinceLastClick > 0 && timeSinceLastClick <= DOUBLE_CLICK_TIME && !hasPointerMoved(newX, newY)) {
                listener.onDoubletap();
            }

            // save new values
            interceptTouchTime = newInterceptTouchTime;
            interceptTouchX = newX;
            interceptTouchY = newY;
        }

        return super.onInterceptTouchEvent(event);
    }

    private boolean hasPointerMoved(float x, float y) {
        boolean movedX = Math.abs(x - interceptTouchX) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        boolean movedY = Math.abs(y - interceptTouchY) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        return movedX || movedY;
    }

    private float distanceSquared(final MotionEvent event) {
        //assert event.getPointerCount() == 2;
        final float dx = event.getX(0) - event.getX(1);
        final float dy = event.getY(0) - event.getY(1);
        return dx*dx + dy*dy;
    }

    public interface Listener {
        void onLongpress();
        void onDoubletap();
        void onPinch();
        void onUnpinch();
    }
}
