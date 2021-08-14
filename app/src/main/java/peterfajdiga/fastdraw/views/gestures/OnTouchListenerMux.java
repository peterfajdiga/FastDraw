package peterfajdiga.fastdraw.views.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

public class OnTouchListenerMux implements View.OnTouchListener {
    private final View.OnTouchListener[] listeners;

    public OnTouchListenerMux(final View.OnTouchListener... listeners) {
        this.listeners = listeners;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        for (final View.OnTouchListener listener : listeners) {
            if (listener.onTouch(v, event)) {
                return true;
            }
        }
        return false;
    }
}
