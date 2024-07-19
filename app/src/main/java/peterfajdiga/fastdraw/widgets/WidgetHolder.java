package peterfajdiga.fastdraw.widgets;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.views.GestureInterceptor;

public class WidgetHolder extends FrameLayout {
    private int margin;
    private OnActionReplaceListener onActionReplaceListener;
    private OnActionConfigureListener onActionConfigureListener;
    private OnWidgetRemovedListener onWidgetRemovedListener;
    private OnEnterEditModeListener onEnterEditModeListener;
    private OnExitEditModeListener onExitEditModeListener;

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
        margin = Math.round(getResources().getDimension(R.dimen.widget_margin));

        inflate(context, R.layout.widget_holder, this);

        final GestureInterceptor widgetContainer = findViewById(R.id.widget_container);
        widgetContainer.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final AppWidgetHostView widgetView = getWidgetView();
            if (widgetView != null) {
                final int widthPx = widgetView.getWidth();
                final int heightPx = widgetView.getHeight();
                final float dp = getResources().getDisplayMetrics().density;
                final int widthDp = Math.round(widthPx / dp);
                final int heightDp = Math.round(heightPx / dp);
                widgetView.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp);
            }
        });

        findViewById(R.id.edit_controls).setOnClickListener(v -> this.exitEditMode());
        findViewById(R.id.action_replace).setOnClickListener(v -> {
            this.exitEditMode();
            if (this.onActionReplaceListener != null) {
                this.onActionReplaceListener.OnActionReplace();
            }
        });
        findViewById(R.id.action_configure).setOnClickListener(v -> {
            this.exitEditMode();
            if (this.onActionConfigureListener != null) {
                this.onActionConfigureListener.OnActionConfigure(getWidgetView());
            }
        });
        findViewById(R.id.action_remove).setOnClickListener(v -> {
            this.exitEditMode();
            this.removeWidgetView();
        });
    }

    public void setWidgetViewGesturesListener(@NonNull final View.OnTouchListener gesturesListener) {
        final GestureInterceptor widgetContainer = findViewById(R.id.widget_container);
        widgetContainer.setOnInterceptTouchListener(gesturesListener);
    }

    public void replaceWidgetView(@NonNull final AppWidgetHostView newWidgetView, final int height) {
        removeWidgetView();

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
        widgetContainer.addView(newWidgetView, layoutParams);

        final View configureButton = findViewById(R.id.action_configure);
        if (WidgetManager.isWidgetReconfigurable(newWidgetView.getAppWidgetInfo())) {
            configureButton.setVisibility(VISIBLE);
        } else {
            configureButton.setVisibility(GONE);
        }
    }

    public void removeWidgetView() {
        final ViewGroup widgetContainer = findViewById(R.id.widget_container);
        final AppWidgetHostView oldWidgetView = getWidgetView();
        if (oldWidgetView != null) {
            widgetContainer.removeView(oldWidgetView);
            if (this.onWidgetRemovedListener != null) {
                this.onWidgetRemovedListener.onWidgetRemoved(oldWidgetView);
            }
        }
    }

    public int getMinWidgetHeight() {
        return findViewById(R.id.edit_controls_toolbar).getHeight() +
            2 * findViewById(R.id.widget_resize_handle).getHeight() -
            2 * margin;
    }

    public void setWidgetHeight(final int newHeight) {
        final AppWidgetHostView widgetView = getWidgetView();
        widgetView.getLayoutParams().height = Math.round(newHeight);
        widgetView.setLayoutParams(widgetView.getLayoutParams());
    }

    public void enterEditMode() {
        final View editControls = findViewById(R.id.edit_controls);
        editControls.setVisibility(VISIBLE);
        if (this.onEnterEditModeListener != null) {
            this.onEnterEditModeListener.onEnterEditMode();
        }
    }

    public void exitEditMode() {
        final View editControls = findViewById(R.id.edit_controls);
        editControls.setVisibility(GONE);
        if (this.onExitEditModeListener != null) {
            this.onExitEditModeListener.onExitEditMode();
        }
    }

    public boolean hasWidgetView() {
        return getWidgetView() != null;
    }

    @Nullable
    private AppWidgetHostView getWidgetView() {
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

    public void setOnActionReplaceListener(@NonNull final OnActionReplaceListener listener) {
        this.onActionReplaceListener = listener;
    }

    public void setOnActionConfigureListener(@NonNull final OnActionConfigureListener listener) {
        this.onActionConfigureListener = listener;
    }

    public void setOnWidgetRemovedListener(@NonNull final OnWidgetRemovedListener listener) {
        this.onWidgetRemovedListener = listener;
    }

    public void setOnEnterEditModeListener(@NonNull final OnEnterEditModeListener listener) {
        this.onEnterEditModeListener = listener;
    }

    public void setOnExitEditModeListener(@NonNull final OnExitEditModeListener listener) {
        this.onExitEditModeListener = listener;
    }

    public interface OnActionReplaceListener {
        void OnActionReplace();
    }

    public interface OnActionConfigureListener {
        void OnActionConfigure(AppWidgetHostView widgetView);
    }

    public interface OnWidgetRemovedListener {
        void onWidgetRemoved(AppWidgetHostView widgetView);
    }

    public interface OnEnterEditModeListener {
        void onEnterEditMode();
    }

    public interface OnExitEditModeListener {
        void onExitEditMode();
    }
}
