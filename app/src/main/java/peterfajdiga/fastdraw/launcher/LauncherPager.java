package peterfajdiga.fastdraw.launcher;

import android.app.WallpaperManager;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.util.Map;

import peterfajdiga.fastdraw.logic.AppItem;
import peterfajdiga.fastdraw.logic.LauncherItem;

public class LauncherPager extends ViewPager {

    public LauncherPager(Context context) {
        super(context);
        initLauncherPager();
    }

    public LauncherPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLauncherPager();
    }

    private void initLauncherPager() {
        setAdapter(new LauncherPagerAdapter());
    }

    // TODO: Do differently
    @Override
    public LauncherPagerAdapter getAdapter() {
        return (LauncherPagerAdapter)super.getAdapter();
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        wallpaperManager.setWallpaperOffsets(getWindowToken(), (position + offset) / (getAdapter().getCount() - 1), 0.5f);
    }

    public void showCategory(String categoryName) {
        final String[] categoryNames = getCategoryNames();
        for (int i = 0; i < categoryNames.length; i++) {
            if (categoryNames[i].equals(categoryName)) {
                setCurrentItem(i);
                return;
            }
        }
    }

    public String getCurrentCategoryName() {
        return getAdapter().getPageTitle(getCurrentItem());
    }

    public String[] getCategoryNames() {
        final Object[] names = getAdapter().categories.keySet().toArray();
        String[] retval = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            retval[i] = (String)names[i];
        }
        return retval;
    }

    public LauncherItem[] getLauncherItems(String category) {
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter)getAdapter().categories.get(category).getAdapter();
        LauncherItem[] items = new LauncherItem[innerAdapter.getCount()];
        for (int i = 0; i < items.length; i++) {
            items[i] = innerAdapter.getItem(i);
        }
        return items;
    }

    public void addLauncherItem(LauncherItem item) {
        final String categoryName = item.getCategory();
        item.setCategoryNoDirty(categoryName);
        CategoryView categoryView = getAdapter().categories.get(categoryName);
        if (categoryView == null) {
            categoryView = new CategoryView(getContext());
            getAdapter().categories.put(categoryName, categoryView);
        }
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter) categoryView.getAdapter();
        innerAdapter.add(item);
        innerAdapter.sort();
        innerAdapter.notifyDataSetChanged();
        getAdapter().notifyDataSetChanged();
    }

    // returns true if the category's last item was removed
    public boolean removeLauncherItem(LauncherItem item, String categoryName) {
        final CategoryView categoryView = getAdapter().categories.get(categoryName);
        //assert appsView != null;
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter) categoryView.getAdapter();
        if (innerAdapter.getCount() == 1) {
            // remove category from pager, no need to remove the item from category
            getAdapter().categories.remove(categoryName);
            getAdapter().notifyDataSetChanged();
            return true;
        } else {
            // remove item from category
            innerAdapter.remove(item);
            innerAdapter.notifyDataSetChanged();
            return false;
        }
    }

    public void moveLauncherItem(LauncherItem item, String categoryName, boolean followItem) {
        String oldCategoryName = item.getCategory();

        boolean lastRemoved = false;
        if (oldCategoryName != null) {
            lastRemoved = removeLauncherItem(item, oldCategoryName);
        }

        item.setCategory(categoryName);
        addLauncherItem(item);
        if (followItem && lastRemoved) {
            showCategory(categoryName);
        }
    }

    public void removeAppItems(String packageName) {
        for (Map.Entry categoryEntry : getAdapter().categories.entrySet()) {
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
                    getAdapter().categories.remove(categoryName);
                }
            }
        }
        getAdapter().notifyDataSetChanged();
    }
}
