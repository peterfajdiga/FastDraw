package peterfajdiga.fastdraw.launcher;

import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.GridView;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Loadable;
import peterfajdiga.fastdraw.launcher.item.Saveable;

public class Category {
    private final CategoryArrayAdapter adapter;
    private final View view;

    public Category(final Context context, final LauncherPager.Owner owner, final LaunchManager launchManager) {
        adapter = new CategoryArrayAdapter(context);
        view = createView(context, owner, launchManager, adapter);
    }

    public View getView() {
        return view;
    }

    public int getItemCount() {
        return adapter.getCount();
    }

    public void addItem(final LauncherItem... launcherItems) {
        for (final LauncherItem launcherItem : launcherItems) {
            adapter.add(launcherItem);
        }
        adapter.sort();
        adapter.notifyDataSetChanged();
    }

    public void removeItem(final LauncherItem launcherItem) {
        adapter.remove(launcherItem);
        adapter.notifyDataSetChanged();
    }

    public void removeItem(@NonNull final Context context, @NonNull final String packageName, final boolean removeShortcuts) {
        boolean itemsRemoved = false;
        for (int i = 0; i < getItemCount();) {
            final LauncherItem item = adapter.getItem(i);
            if ((removeShortcuts || !(item instanceof Saveable)) && packageName.equals(item.getPackageName())) {
                removeItem(item);
                itemsRemoved = true;
                if (item instanceof Saveable) {
                    ShortcutItemManager.deleteShortcut(context, (Saveable)item); // TODO: refactor?
                }
            } else {
                i++;
            }
        }

        if (itemsRemoved) {
            adapter.notifyDataSetChanged();
        }
    }

    @NonNull
    public LauncherItem[] getItems() {
        final LauncherItem[] items = new LauncherItem[getItemCount()];
        for (int i = 0; i < items.length; i++) {
            items[i] = adapter.getItem(i);
        }
        return items;
    }

    private View createView(final Context context, final LauncherPager.Owner owner, final LaunchManager launchManager, final CategoryArrayAdapter adapter) {
        final CategoryView categoryView = new CategoryView(context);

        if (Preferences.appItemResource == R.layout.app_item_grid) {
            categoryView.setNumColumns(GridView.AUTO_FIT);
            categoryView.setColumnWidth(
                context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_icon_size) +
                    context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_icon_padding) * 2
            );
            final int padding = Math.round(context.getResources().getDimensionPixelSize(R.dimen.app_item_grid_container_padding));
            categoryView.setPadding(padding, padding, padding, padding);
            categoryView.setClipToPadding(false);
            categoryView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        }
        categoryView.setStackFromBottom(Preferences.stackFromBottom);

        categoryView.setAdapter(adapter);

        categoryView.setOnItemClickListener((adapterView, view, pos, id) -> {
            final LauncherItem item = adapter.getItem(pos);

            ActivityOptions opts;
            if (Build.VERSION.SDK_INT >= 23) {
                opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());
            } else {
                opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
            }

            item.launch(context, launchManager, opts.toBundle(), view.getClipBounds());
        });

        final PointF touchPoint = new PointF();
        categoryView.setOnTouchListener((v, event) -> {
            touchPoint.set(event.getX(), event.getY());
            return false;
        });
        categoryView.setOnItemLongClickListener((parent, view, position, id) -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

            // start drag
            final View.DragShadowBuilder shadow = new OffsetDragShadowBuilder(view, touchPoint.x, touchPoint.y);
            if (Build.VERSION.SDK_INT < 24) {
                view.startDrag(null, shadow, null, 0);
            } else {
                view.startDragAndDrop(null, shadow, null, 0);
            }
            owner.onDragStarted(view, (LauncherItem)parent.getItemAtPosition(position));

            return false;
        });

        categoryView.setListener(new CategoryView.Listener() {
            @Override
            public void onLongpress() {
                owner.onPagerLongpress();
            }

            @Override
            public void onDoubletap() {
                owner.onPagerDoubletap();
            }

            @Override
            public void onPinch() {
                owner.onPagerPinch();
            }

            @Override
            public void onUnpinch() {
                owner.onPagerUnpinch();
            }
        });

        return categoryView;
    }

    public void loadItems(final Context context) {
        final int n = adapter.getCount();
        for (int i = 0; i < n; i++) {
            final LauncherItem item = adapter.getItem(i);
            if (item instanceof Loadable) {
                ((Loadable)item).load(context);
            }
        }
        adapter.sort();
        adapter.notifyDataSetChanged();
    }

    public void loadItemsAsync() {
        final ItemLoader itemLoader = new ItemLoader();
        itemLoader.execute();
    }

    private void reportIfLoadFailure() {
        final int n = adapter.getCount();
        for (int i = 0; i < n; i++) {
            final LauncherItem item = adapter.getItem(i);
            if (item instanceof Loadable && !((Loadable)item).isLoaded()) {
                Log.e("CategoryArrayAdapter", "Could not load launcher item: " + item.getID());
                // TODO (BUG): retry?
                return;
            }
        }
    }

    private final class ItemLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                final Context context = adapter.getContext();
                final int n = adapter.getCount();
                for (int i = 0; i < n; i++) {
                    final LauncherItem item = adapter.getItem(i);
                    if (item instanceof Loadable) {
                        ((Loadable)item).load(context);
                    }
                }
            } catch (Exception e) {
                Log.e("FastDraw_itemLoad", e.toString());
            }
            return null;
        }

        /**
         * this must be called on main thread
         */
        @Override
        protected void onPostExecute(Void result) {
            reportIfLoadFailure();
            adapter.sort();
            adapter.notifyDataSetChanged();
        }
    }
}
