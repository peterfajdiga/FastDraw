package peterfajdiga.fastdraw.launcher;

import android.graphics.Point;
import android.view.View;

public class OffsetDragShadowBuilder extends View.DragShadowBuilder {
    int touchX, touchY;

    /**
     * @param view The dragged view
     * @param x    The x coordinate of the touch (screen space)
     * @param y    The y coordinate of the touch (screen space)
     */
    public OffsetDragShadowBuilder(final View view, final float x, final float y) {
        super(view);
        touchX = (int)(x - view.getX());
        touchY = (int)(y - view.getY());
        if (touchX < 0) {
            touchX = 0; // avoid crash when user offsets the shadow by using two fingers
        }
        if (touchY < 0) {
            touchY = 0; // avoid crash when user offsets the shadow by using two fingers
        }
    }

    @Override
    public void onProvideShadowMetrics(final Point outShadowSize, final Point outShadowTouchPoint) {
        super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
        outShadowTouchPoint.x = touchX;
        outShadowTouchPoint.y = touchY;
    }
}
