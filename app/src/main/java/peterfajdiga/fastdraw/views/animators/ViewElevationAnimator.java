package peterfajdiga.fastdraw.views.animators;

import android.animation.ValueAnimator;
import android.view.View;

public class ViewElevationAnimator implements ValueAnimator.AnimatorUpdateListener {
    private final View view;
    private final float elevationStart;
    private final float elevationDelta;

    public ViewElevationAnimator(final View view, final float elevationStart, final float elevationEnd) {
        this.view = view;
        this.elevationStart = elevationStart;
        this.elevationDelta = elevationEnd - elevationStart;
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animator) {
        view.setElevation(elevationStart + elevationDelta * animator.getAnimatedFraction());
    }
}
