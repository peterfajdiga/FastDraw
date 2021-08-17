package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.views.gestures.DoubleTap;
import peterfajdiga.fastdraw.views.gestures.LongPress;
import peterfajdiga.fastdraw.views.gestures.OnTouchListenerMux;
import peterfajdiga.fastdraw.views.gestures.Pinch;

public class Category {
    private final CategoryAdapter adapter;
    private final View view;

    public Category(final Context context, final Launcher.Owner owner, final LaunchManager launchManager) {
        this.adapter = new CategoryAdapter(owner, launchManager);
        view = createView(context, owner, adapter);
    }

    public View getView() {
        return view;
    }

    public int getItemCount() {
        return adapter.getItemCount();
    }

    public void addItems(final LauncherItem... launcherItems) {
        adapter.getItems().addAll(Arrays.asList(launcherItems));
    }

    public void removeItem(final LauncherItem launcherItem) {
        adapter.getItems().remove(launcherItem);
    }

    @NonNull
    public LauncherItem[] getItems() {
        final LauncherItem[] items = new LauncherItem[getItemCount()];
        for (int i = 0; i < items.length; i++) {
            items[i] = adapter.getItems().get(i);
        }
        return items;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(
        final Context context,
        final Launcher.Owner owner,
        final CategoryAdapter adapter
    ) {
        final RecyclerView containerView = new RecyclerView(context);
        containerView.setAdapter(adapter);

        if (Preferences.appItemResource == R.layout.app_item_grid) {
            if (Preferences.stackFromBottom) {
                containerView.setLayoutManager(new GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, true)); // TODO: configurable or automatic column count
            } else {
                containerView.setLayoutManager(new GridLayoutManager(context, 4)); // TODO: configurable or automatic column count
            }
            final int padding = Math.round(context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_container_padding));
            containerView.setPadding(padding, padding, padding, padding);
            containerView.setClipToPadding(false);
            containerView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        } else {
            containerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        }

        containerView.setOnTouchListener(new OnTouchListenerMux(
            new LongPress(1, owner::onPagerLongpress),
            new DoubleTap(1, owner::onPagerDoubletap),
            new Pinch(false, owner::onPagerPinch),
            new Pinch(true, owner::onPagerUnpinch)
        ));
        return containerView;
    }
}
