package peterfajdiga.fastdraw;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

import androidx.annotation.NonNull;

public class ShadowDrawable extends DrawableWrapper {
    public ShadowDrawable(@NonNull final Drawable foreground) {
        super(foreground);
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        canvas.save();
        canvas.translate(0, 2);
        super.setTint(0x80000000);
        super.draw(canvas);
        canvas.restore();

        super.setTint(0xffffffff);
        super.draw(canvas);
    }
}
