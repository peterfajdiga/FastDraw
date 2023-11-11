package peterfajdiga.fastdraw;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class ShadowDrawable extends DrawableWrapper {
    @ColorInt private final int shadowTint;
    @NonNull private final PointF shadowOffset;

    public ShadowDrawable(@NonNull final Drawable foreground, @ColorInt final int shadowTint, @NonNull final PointF shadowOffset) {
        super(foreground);
        this.shadowTint = shadowTint;
        this.shadowOffset = shadowOffset;
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        canvas.save();
        canvas.translate(shadowOffset.x, shadowOffset.y);
        super.setTint(shadowTint);
        super.draw(canvas);
        canvas.restore();

        super.setTintList(null);
        super.draw(canvas);
    }
}
