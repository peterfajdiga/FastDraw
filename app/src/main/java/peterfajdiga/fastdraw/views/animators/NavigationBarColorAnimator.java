package peterfajdiga.fastdraw.views.animators;

import android.animation.ValueAnimator;
import android.view.Window;

public class NavigationBarColorAnimator implements ValueAnimator.AnimatorUpdateListener {
    private final Window window;

    public NavigationBarColorAnimator(final Window window) {
        this.window = window;
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animator) {
        final int color = (Integer)animator.getAnimatedValue();
        window.setNavigationBarColor(color);
    }
}
