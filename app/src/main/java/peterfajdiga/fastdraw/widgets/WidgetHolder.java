package peterfajdiga.fastdraw.widgets;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.views.GestureInterceptor;

public class WidgetHolder extends FrameLayout {
    private WidgetManager widgetManager;

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

    public void setup(@NonNull final WidgetManager widgetManager, @NonNull final View.OnTouchListener gesturesListener) {
        this.widgetManager = widgetManager;

        final GestureInterceptor widgetContainer = findViewById(R.id.widget_container);
        widgetContainer.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final AppWidgetHostView widgetView = getCurrentWidgetView();
            if (widgetView != null) {
                final int widthPx = widgetView.getWidth();
                final int heightPx = widgetView.getHeight();
                final float dp = getResources().getDisplayMetrics().density;
                final int widthDp = Math.round(widthPx / dp);
                final int heightDp = Math.round(heightPx / dp);
                widgetView.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp);
            }
        });
        widgetContainer.setOnInterceptTouchListener(gesturesListener);
    }

    public void replaceWidgetView(@NonNull final AppWidgetHostView widgetView) {
        removeWidgetView();

        final Resources res = getResources();
        final float height = Math.min(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                500, // TODO: read from preferences
                res.getDisplayMetrics()
            ),
            res.getDisplayMetrics().heightPixels * 0.75f // TODO: handle landscape orientation
        );
        final int margin = Math.round(res.getDimension(R.dimen.widget_margin));

        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            Math.round(height),
            Gravity.CENTER
        );
        layoutParams.topMargin = margin;
        layoutParams.bottomMargin = margin;
        layoutParams.leftMargin = margin;
        layoutParams.rightMargin = margin;

        final ViewGroup widgetContainer = findViewById(R.id.widget_container);
        widgetContainer.addView(widgetView, layoutParams);
    }

    public void removeWidgetView() {
        final ViewGroup widgetContainer = findViewById(R.id.widget_container);
        final AppWidgetHostView oldWidgetView = getCurrentWidgetView();
        if (oldWidgetView != null) {
            widgetManager.deleteWidget(oldWidgetView.getAppWidgetId());
            widgetContainer.removeView(oldWidgetView);
        }
    }

    public void setWidgetHeight(final int newHeight) {
        final AppWidgetHostView widgetView = getCurrentWidgetView();
        widgetView.getLayoutParams().height = Math.round(newHeight);
        widgetView.setLayoutParams(widgetView.getLayoutParams());
    }

    @Nullable
    private AppWidgetHostView getCurrentWidgetView() {
        final ViewGroup widgetContainer = findViewById(R.id.widget_container);
        final int n = widgetContainer.getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = widgetContainer.getChildAt(i);
            if (child instanceof AppWidgetHostView) {
                return (AppWidgetHostView)child;
            }
        }
        return null;
    }
}
