package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.TreeMap;

import peterfajdiga.fastdraw.logic.AppItem;
import peterfajdiga.fastdraw.logic.LauncherItem;

public class LauncherPagerAdapter extends PagerAdapter {

    private Map<String, CategoryView> categories = new TreeMap<>();
    private Context context;

    public LauncherPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final CategoryView layout = (CategoryView)categories.values().toArray()[position];
        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View)view);
    }

    @Override
    public int getCount() {
        return categories.keySet().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public String getPageTitle(int position) {
        return (String)categories.keySet().toArray()[position];
    }

    @Override
    public int getItemPosition(Object object) {
        final Object[] views = categories.values().toArray();
        for (int i = 0; i < views.length; i++) {
            if (views[i] == object) {
                return i;
            }
        }
        return POSITION_NONE;
    }


    public String[] getCategoryNames() {
        final Object[] names = categories.keySet().toArray();
        String[] retval = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            retval[i] = (String)names[i];
        }
        return retval;
    }

    public LauncherItem[] getLauncherItems(String category) {
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter)categories.get(category).getAdapter();
        LauncherItem[] items = new LauncherItem[innerAdapter.getCount()];
        for (int i = 0; i < items.length; i++) {
            items[i] = innerAdapter.getItem(i);
        }
        return items;
    }

    public void addLauncherItem(LauncherItem item, String category) {
        CategoryView categoryView = categories.get(category);
        if (categoryView == null) {
            categoryView = new CategoryView(context);
            categories.put(category, categoryView);
        }
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter) categoryView.getAdapter();
        innerAdapter.add(item);
        innerAdapter.sort();
        innerAdapter.notifyDataSetChanged();
        notifyDataSetChanged();
    }

    // returns true if the category's last item was removed
    public boolean removeLauncherItem(LauncherItem item, String categoryName) {
        final CategoryView categoryView = categories.get(categoryName);
        //assert appsView != null;
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter) categoryView.getAdapter();
        if (innerAdapter.getCount() == 1) {
            // remove category from pager, no need to remove the item from category
            categories.remove(categoryName);
            notifyDataSetChanged();
            return true;
        } else {
            // remove item from category
            innerAdapter.remove(item);
            innerAdapter.notifyDataSetChanged();
            return false;
        }
    }

    public void removeAppItems(String packageName) {
        for (Map.Entry categoryEntry : categories.entrySet()) {
            String categoryName = (String)categoryEntry.getKey();
            CategoryView categoryView = (CategoryView)categoryEntry.getValue();

            CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter)categoryView.getAdapter();
            boolean itemsRemoved = false;
            // iterate through categories' items
            for (int i = 0; i < innerAdapter.getCount();) {
                LauncherItem launcherItem = innerAdapter.getItem(i);
                if (launcherItem instanceof AppItem && ((AppItem)launcherItem).packageName.equals(packageName)) {
                    // remove matching items
                    // don't increment i in this case, as item count has decreased
                    innerAdapter.remove(launcherItem);
                    itemsRemoved = true;
                } else {
                    i++;
                }
            }
            if (itemsRemoved) {
                // update category adapter
                innerAdapter.notifyDataSetChanged();
                if (innerAdapter.getCount() == 0) {
                    // remove the now empty category from pager
                    categories.remove(categoryName);
                }
            }
        }
        notifyDataSetChanged();
    }
}
