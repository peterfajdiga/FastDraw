package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.Arrays;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.views.AutoGridLayoutManager;
import peterfajdiga.fastdraw.views.gestures.DoubleTap;
import peterfajdiga.fastdraw.views.gestures.LongPress;
import peterfajdiga.fastdraw.views.gestures.OnTouchListenerMux;
import peterfajdiga.fastdraw.views.gestures.Pinch;
import peterfajdiga.fastdraw.views.gestures.Swipe;

public class Category {
    private final CategoryAdapter adapter;
    private final View view;

    public Category(final Context context, final Launcher.Listener listener, final LaunchManager launchManager) {
        this.adapter = new CategoryAdapter(listener, launchManager);
        view = createView(context, listener, adapter);
    }

    public View getView() {
        return view;
    }

    public int getItemCount() {
        return adapter.getItemCount();
    }

    public void addItems(final DisplayItem... displayItems) {
        adapter.getItems().addAll(Arrays.asList(displayItems));
    }

    public void removeItem(final String id) {
        final SortedList<DisplayItem> items = adapter.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(id)) {
                items.removeItemAt(i);
                return;
            }
        }
    }

    @NonNull
    public LauncherItem[] getItems() {
        final LauncherItem[] items = new LauncherItem[getItemCount()];
        for (int i = 0; i < items.length; i++) {
            items[i] = adapter.getItems().get(i).source;
        }
        return items;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(
        final Context context,
        final Launcher.Listener listener,
        final CategoryAdapter adapter
    ) {
        final RecyclerView containerView = new RecyclerView(context);
        containerView.setAdapter(adapter);

        if (!Preferences.appsLinearList) {
            final int spanWidth = context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_icon_size) +
                context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_icon_padding) * 2;
            containerView.setLayoutManager(new AutoGridLayoutManager(context, spanWidth, GridLayoutManager.VERTICAL, Preferences.stackFromBottom));
            final int padding = Math.round(context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_container_padding));
            containerView.setPadding(padding, padding, padding, padding);
            containerView.setClipToPadding(false);
            containerView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        } else {
            containerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        }

        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        containerView.setOnTouchListener(new OnTouchListenerMux(
            new LongPress(displayMetrics, listener::onLongpress),
            new DoubleTap(displayMetrics, listener::onDoubletap),
            new Pinch(displayMetrics, false, listener::onPinch),
            new Pinch(displayMetrics, true, listener::onUnpinch),
            new Swipe(displayMetrics, Swipe.Direction.UP, 2, listener::onSwipeUp2F),
            new Swipe(displayMetrics, Swipe.Direction.DOWN, 2, listener::onSwipeDown2F)
        ));
        return containerView;
    }
}
