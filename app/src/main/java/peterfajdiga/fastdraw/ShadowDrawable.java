package peterfajdiga.fastdraw;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class ShadowDrawable extends DrawableWrapper {
    @ColorInt private final int shadowTint;

    public ShadowDrawable(@NonNull final Drawable foreground, @ColorInt int shadowTint) {
        super(foreground);
        this.shadowTint = shadowTint;
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        canvas.save();
        canvas.translate(0, 2);
        super.setTint(shadowTint);
        super.draw(canvas);
        canvas.restore();

        super.setTintList(null);
        super.draw(canvas);
    }
}
