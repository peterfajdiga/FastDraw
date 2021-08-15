package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.concurrent.Executors;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Loadable;
import peterfajdiga.fastdraw.launcher.item.Saveable;
import peterfajdiga.fastdraw.views.gestures.DoubleTap;
import peterfajdiga.fastdraw.views.gestures.LongPress;
import peterfajdiga.fastdraw.views.gestures.OnTouchListenerMux;
import peterfajdiga.fastdraw.views.gestures.Pinch;

public class Category {
    private final CategoryAdapter adapter;
    private final View view;
    private boolean loaded = false;

    public Category(final Context context, final LauncherPager.Owner owner, final LaunchManager launchManager) {
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
        if (loaded) {
            for (final LauncherItem item : launcherItems) {
                if (item instanceof Loadable && !((Loadable)item).isLoaded()) {
                    Log.w("Category", "Adding a non-loaded item " + item.getPackageName() + " to a loaded category");
                }
            }
        }
        adapter.getItems().addAll(Arrays.asList(launcherItems));
    }

    public void removeItem(final LauncherItem launcherItem) {
        adapter.getItems().remove(launcherItem);
    }

    public void removeItem(@NonNull final Context context, @NonNull final String packageName, final boolean removeShortcuts) {
        for (int i = 0; i < getItemCount();) {
            final LauncherItem item = adapter.getItems().get(i);
            if ((removeShortcuts || !(item instanceof Saveable)) && packageName.equals(item.getPackageName())) {
                adapter.getItems().removeItemAt(i);
                if (item instanceof Saveable) {
                    ShortcutItemManager.deleteShortcut(context, (Saveable)item); // TODO: refactor?
                }
            } else {
                i++;
            }
        }
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
        final LauncherPager.Owner owner,
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

    public void loadItems(final Context context) {
        loadItemsHelper(context);
        adapter.getItems().replaceAll(getItems());
    }

    public void loadItemsAsync(final Context context) {
        final Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            loadItemsHelper(context);
            handler.post(() -> adapter.getItems().replaceAll(getItems()));
        });
    }

    private void loadItemsHelper(final Context context) {
        loaded = true;
        final LauncherItem[] items = getItems();
        for (final LauncherItem item : items) {
            if (item instanceof Loadable) {
                try {
                    ((Loadable)item).load(context);
                } catch (final Exception e) {
                    Log.e("Category", "Failed to load item of package " + item.getPackageName(), e);
                }
            }
        }
    }
}
