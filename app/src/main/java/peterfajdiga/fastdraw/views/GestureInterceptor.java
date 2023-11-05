package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GestureInterceptor extends FrameLayout {
    View.OnTouchListener listener;

    public GestureInterceptor(@NonNull final Context context) {
        super(context);
    }

    public GestureInterceptor(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureInterceptor(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GestureInterceptor(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnInterceptTouchListener(final View.OnTouchListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        return listener.onTouch(this, event);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        listener.onTouch(this, event);
        return true;
    }
}
