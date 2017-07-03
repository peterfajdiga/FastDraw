package peterfajdiga.fastdraw;

import android.animation.ValueAnimator;
import android.view.Window;

public class NavigationBarAnimator implements ValueAnimator.AnimatorUpdateListener {

    private Window window;

    public NavigationBarAnimator(Window window) {
        this.window = window;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        final int color = (Integer)animator.getAnimatedValue();
        window.setNavigationBarColor(color);
    }
}
