package peterfajdiga.fastdraw.views.gestures;

import android.view.View;

public interface Gesture extends View.OnTouchListener {
    void cancel();
}
