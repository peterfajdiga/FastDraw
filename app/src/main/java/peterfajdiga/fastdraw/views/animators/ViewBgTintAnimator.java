package peterfajdiga.fastdraw.views.animators;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.view.View;

public class ViewBgTintAnimator implements ValueAnimator.AnimatorUpdateListener {
    private final View view;

    public ViewBgTintAnimator(final View view) {
        this.view = view;
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animator) {
        final int color = (Integer)animator.getAnimatedValue();
        view.setBackgroundTintList(ColorStateList.valueOf(color));
        // NOTE: don't do `view.getBackground().setTint()`, because that tints *everything* that uses the same drawable resource
        // (or any drawable resource referenced by `view.getBackground` drawable resource)
    }
}
