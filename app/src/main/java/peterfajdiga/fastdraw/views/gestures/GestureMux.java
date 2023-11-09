package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

public class GestureMux implements Gesture {
    private final Gesture[] gestures;

    public GestureMux(final Gesture... gestures) {
        this.gestures = gestures;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        for (final Gesture gesture : gestures) {
            if (gesture.onTouch(v, event)) {
                cancel();
                return true;
            }
        }
        return false;
    }

    @Override
    public void cancel() {
        for (final Gesture gesture : gestures) {
            gesture.cancel();
        }
    }
}
