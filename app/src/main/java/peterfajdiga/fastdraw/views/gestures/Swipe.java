package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.function.BooleanSupplier;

public class Swipe implements Gesture {
    private final float threshold;
    private final Direction direction;
    private final GestureListener listener;
    final BooleanSupplier condition;

    private boolean started;
    private final PointF startPoint = new PointF();
    private final PointF lastPoint = new PointF();

    public Swipe(
        final Direction direction,
        final float threshold,
        final GestureListener listener,
        final BooleanSupplier condition
    ) {
        this.direction = direction;
        this.threshold = threshold;
        this.listener = listener;
        this.condition = condition;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getPointerCount() != 1) {
            return false;
        }

        if (!condition.getAsBoolean()) {
            cancel();
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                start(event);
                return false;
            case MotionEvent.ACTION_MOVE:
                maybeCancel(event);
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
        startPoint.set(event.getX(), event.getY());
        lastPoint.set(event.getX(), event.getY());
        started = true;
    }

    @Override
    public void cancel() {
        started = false;
    }

    private void maybeCancel(final MotionEvent event) {
        final boolean correctDirection =
            direction == Direction.UP    && event.getY() <= lastPoint.y ||
            direction == Direction.DOWN  && event.getY() >= lastPoint.y ||
            direction == Direction.LEFT  && event.getX() <= lastPoint.x ||
            direction == Direction.RIGHT && event.getX() >= lastPoint.x;
        if (!correctDirection) {
            startPoint.set(event.getX(), event.getY());
        }
        lastPoint.set(event.getX(), event.getY());
    }

    private boolean maybeFinish(final MotionEvent event) {
        final boolean finished = started && (
            direction == Direction.UP    && event.getY() - startPoint.y < -threshold ||
            direction == Direction.DOWN  && event.getY() - startPoint.y >  threshold ||
            direction == Direction.LEFT  && event.getX() - startPoint.x < -threshold ||
            direction == Direction.RIGHT && event.getX() - startPoint.x >  threshold
        );
        if (finished) {
            listener.onGesture();
        }
        cancel();
        return finished;
    }

    public enum Direction {UP, DOWN, LEFT, RIGHT}
}
