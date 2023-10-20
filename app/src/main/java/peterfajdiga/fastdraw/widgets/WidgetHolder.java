package peterfajdiga.fastdraw.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.R;

public class WidgetHolder extends FrameLayout {
    public WidgetHolder(@NonNull final Context context) {
        super(context);
        init(context);
    }

    public WidgetHolder(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WidgetHolder(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public WidgetHolder(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(@NonNull final Context context) {
        inflate(context, R.layout.widget_holder, this);
    }
}
