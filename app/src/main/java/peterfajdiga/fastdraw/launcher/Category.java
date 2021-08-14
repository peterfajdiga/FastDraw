package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Loadable;
import peterfajdiga.fastdraw.launcher.item.Saveable;

public class Category {
    private final CategoryAdapter adapter;
    private final View view;

    public Category(final Context context, final LauncherPager.Owner owner, final LaunchManager launchManager) {
        this.adapter = new CategoryAdapter(owner, launchManager);
        view = createView(context, owner, launchManager, adapter); // TODO: reduce parameters
    }

    public View getView() {
        return view;
    }

    public int getItemCount() {
        return adapter.getItemCount();
    }

    public void addItem(final LauncherItem... launcherItems) {
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

    private View createView(final Context context, final LauncherPager.Owner owner, final LaunchManager launchManager, final CategoryAdapter adapter) {
        final RecyclerView containerView = new RecyclerView(context);
        containerView.setAdapter(adapter);

        if (Preferences.appItemResource == R.layout.app_item_grid) {
            containerView.setLayoutManager(new GridLayoutManager(context, 4)); // TODO: configurable or automatic column count
        } else {
            containerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        }

        return containerView;
    }

    public void loadItems(final Context context) {
        final LauncherItem[] items = getItems();
        for (final LauncherItem item : items) {
            if (item instanceof Loadable) {
                final int currentIndex = adapter.getItems().indexOf(item);
                ((Loadable)item).load(context);
                adapter.getItems().updateItemAt(currentIndex, item);
            }
        }
    }

    public void loadItemsAsync() {
        final ItemLoader itemLoader = new ItemLoader();
        itemLoader.execute();
    }

    private void reportIfLoadFailure() {
        final int n = getItemCount();
        for (int i = 0; i < n; i++) {
            final LauncherItem item = adapter.getItems().get(i);
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
                final Context context = view.getContext(); // TODO: refactor?
                loadItems(context);
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
        }
    }
}
