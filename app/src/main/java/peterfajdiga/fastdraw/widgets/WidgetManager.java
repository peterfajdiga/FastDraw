package peterfajdiga.fastdraw.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WidgetManager {
    private final Activity activity;
    private final AppWidgetHost widgetHost;
    private final AppWidgetManager widgetManager;
    private final int widgetPickRequestCode;
    private final int widgetCreateRequestCode;

    public WidgetManager(
        @NonNull final Activity activity,
        final int widgetHostId,
        final int widgetPickRequestCode,
        final int widgetCreateRequestCode
    ) {
        this.activity = activity;
        this.widgetHost = new AppWidgetHost(activity, widgetHostId);
        this.widgetManager = AppWidgetManager.getInstance(activity);
        this.widgetPickRequestCode = widgetPickRequestCode;
        this.widgetCreateRequestCode = widgetCreateRequestCode;
    }

    public void startListening() {
        widgetHost.startListening();
    }

    public void stopListening() {
        widgetHost.stopListening();
    }

    // TODO: use bottom sheet if possible
    public void pickWidget() {
        final int widgetId = widgetHost.allocateAppWidgetId();
        final Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        activity.startActivityForResult(pickIntent, widgetPickRequestCode);
    }

    @Nullable
    public AppWidgetHostView createOrConfigureWidgetView(final int widgetId) {
        final AppWidgetProviderInfo widgetInfo = widgetManager.getAppWidgetInfo(widgetId);
        if (widgetInfo.configure != null) {
            final Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(widgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            activity.startActivityForResult(intent, widgetCreateRequestCode);
            return null;
        } else {
            return createWidgetView(widgetId);
        }
    }

    @Nullable
    public AppWidgetHostView createWidgetView(final int widgetId) {
        final AppWidgetProviderInfo appWidgetInfo = widgetManager.getAppWidgetInfo(widgetId);
        final AppWidgetHostView view = widgetHost.createView(activity, widgetId, appWidgetInfo);
        view.setAppWidget(widgetId, appWidgetInfo);
        return view;
    }

    public void deleteWidget(final int widgetId) {
        widgetHost.deleteAppWidgetId(widgetId);
    }
}
