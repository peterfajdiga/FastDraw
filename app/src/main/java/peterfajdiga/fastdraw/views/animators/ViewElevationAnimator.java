package peterfajdiga.fastdraw.views.animators;

import android.animation.ValueAnimator;
import android.view.View;

public class ViewElevationAnimator implements ValueAnimator.AnimatorUpdateListener {
    private final View view;

    public ViewElevationAnimator(final View view) {
        this.view = view;
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animator) {
        view.setElevation((float)animator.getAnimatedValue());
    }
}
