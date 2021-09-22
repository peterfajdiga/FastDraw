package peterfajdiga.fastdraw.views.animators;

import android.animation.ValueAnimator;
import android.view.View;

public class ViewBgColorAnimator implements ValueAnimator.AnimatorUpdateListener {
    private final View view;

    public ViewBgColorAnimator(final View view) {
        this.view = view;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        final int color = (Integer)animator.getAnimatedValue();
        view.setBackgroundColor(color);
    }
}
