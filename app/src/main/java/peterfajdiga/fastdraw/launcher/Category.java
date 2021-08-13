package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Loadable;
import peterfajdiga.fastdraw.launcher.item.Saveable;

public class Category {
    private final CategoryArrayAdapter adapter; // TODO: remove
    private final CategoryView view;

    public Category(final Context context, final LaunchManager launchManager) {
        view = new CategoryView(context, launchManager);
        adapter = (CategoryArrayAdapter)view.getAdapter();
    }

    public View getView() {
        return view;
    }

    public int getCount() {
        return adapter.getCount();
    }

    public void add(final LauncherItem... launcherItems) {
        for (final LauncherItem launcherItem : launcherItems) {
            adapter.add(launcherItem);
        }
        adapter.sort();
        adapter.notifyDataSetChanged();
    }

    public void remove(final LauncherItem launcherItem) {
        adapter.remove(launcherItem);
        adapter.notifyDataSetChanged();
    }

    public void remove(@NonNull final String packageName, final boolean removeShortcuts) {
        boolean itemsRemoved = false;
        for (int i = 0; i < getCount();) {
            final LauncherItem item = getItem(i);
            if ((removeShortcuts || !(item instanceof Saveable)) && packageName.equals(item.getPackageName())) {
                remove(item);
                // TODO: delete shortcut files
                itemsRemoved = true;
            } else {
                i++;
            }
        }

        if (itemsRemoved) {
            adapter.notifyDataSetChanged();
        }
    }

    public LauncherItem getItem(final int index) {
        return adapter.getItem(index);
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
