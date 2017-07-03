package peterfajdiga.fastdraw;

import android.animation.ValueAnimator;
import android.view.View;

public class ViewBgAnimator implements ValueAnimator.AnimatorUpdateListener {

    private View view;

    public ViewBgAnimator(View view) {
        this.view = view;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        final int color = (Integer)animator.getAnimatedValue();
        view.setBackgroundColor(color);
    }
}
