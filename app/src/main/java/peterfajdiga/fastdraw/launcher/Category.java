package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import peterfajdiga.fastdraw.Postable;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;
import peterfajdiga.fastdraw.views.AutoGridLayoutManager;
import peterfajdiga.fastdraw.views.NestedScrollChildManager;

public class Category {
    public final NestedScrollChildManager nestedScrollChildManager = new NestedScrollChildManager();
    private final CategoryAdapter adapter;
    private final View view;

    public Category(
        final Context context,
        final LaunchManager launchManager,
        final Postable dragEndService,
        final View.OnTouchListener backgroundTouchListener
    ) {
        this.adapter = new CategoryAdapter(launchManager, dragEndService);
        view = createView(context, backgroundTouchListener, adapter, nestedScrollChildManager);
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
        final int index = findItemIndex(items, id);
        if (index != -1) {
            items.removeItemAt(index);
        }
    }

    public void updateItem(final String id, final DisplayItem updatedItem) {
        final SortedList<DisplayItem> items = adapter.getItems();
        final int index = findItemIndex(items, id);
        if (index != -1) {
            items.updateItemAt(index, updatedItem);
        }
    }

    private static int findItemIndex(final SortedList<DisplayItem> items, final String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    public List<LauncherItem> getItems() {
        final int n = getItemCount();
        final List<LauncherItem> items = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            items.add(adapter.getItems().get(i).source);
        }
        return items;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View createView(
        final Context context,
        final View.OnTouchListener backgroundTouchListener,
        final CategoryAdapter adapter,
        final NestedScrollChildManager nestedScrollChildManager
    ) {
        final RecyclerView containerView = new RecyclerView(context);
        containerView.setAdapter(adapter);

        containerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        final int spanWidth = context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_icon_size) +
            context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_icon_padding) * 2;
        final GridLayoutManager layoutManager = new AutoGridLayoutManager(context, spanWidth, GridLayoutManager.VERTICAL, false);
        containerView.setLayoutManager(layoutManager);
        final int padding = Math.round(context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_container_padding));
        containerView.setPadding(padding, padding, padding, padding);
        containerView.setClipToPadding(false);
        containerView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        nestedScrollChildManager.setup(containerView, () -> {
            final int lineCount = (int)Math.ceil((double)adapter.getItemCount() / (double)layoutManager.getSpanCount());
            return lineCount * getItemHeight(layoutManager) + 2 * padding;
        });

        containerView.setOnTouchListener(backgroundTouchListener);
        return containerView;
    }

    private static int getItemHeight(@NonNull final RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.getChildCount() == 0) {
            return 0;
        }

        final View child = layoutManager.getChildAt(0);
        if (child == null) {
            return 0;
        }

        return child.getHeight();
    }
}
