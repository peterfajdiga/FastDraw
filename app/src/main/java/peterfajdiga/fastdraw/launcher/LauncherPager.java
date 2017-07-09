package peterfajdiga.fastdraw.launcher;

import android.app.WallpaperManager;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import peterfajdiga.fastdraw.launcher.item.LauncherItem;

public class LauncherPager extends ViewPager {

    public LauncherPager(Context context) {
        super(context);
        init();
    }

    public LauncherPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setAdapter(new LauncherPagerAdapter());
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        wallpaperManager.setWallpaperOffsets(getWindowToken(), (position + offset) / (getAdapter().getCount() - 1), 0.5f);
    }

    // returns true if category exists and was selected
    public boolean showCategory(String categoryName) {
        final String[] categoryNames = getCategoryNames();
        for (int i = 0; i < categoryNames.length; i++) {
            if (categoryNames[i].equals(categoryName)) {
                setCurrentItem(i);
                return true;
            }
        }
        return false;
    }

    public String getCurrentCategoryName() {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        return adapter.getPageTitle(getCurrentItem());
    }

    public String[] getCategoryNames() {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        final Object[] names = adapter.categories.keySet().toArray();
        String[] retval = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            retval[i] = (String)names[i];
        }
        return retval;
    }

    public LauncherItem[] getLauncherItems(String category) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter)adapter.categories.get(category).getAdapter();
        LauncherItem[] items = new LauncherItem[innerAdapter.getCount()];
        for (int i = 0; i < items.length; i++) {
            items[i] = innerAdapter.getItem(i);
        }
        return items;
    }

    public void addLauncherItemBulk(LauncherItem item) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        final String categoryName = item.getCategory();
        item.setCategoryNoDirty(categoryName);
        CategoryView categoryView = adapter.categories.get(categoryName);
        if (categoryView == null) {
            categoryView = new CategoryView(getContext());
            adapter.categories.put(categoryName, categoryView);
        }
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter) categoryView.getAdapter();
        innerAdapter.add(item);
    }

    public void finishBulk() {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)getAdapter();
        for (CategoryView categoryView : adapter.categories.values()) {
            final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter)categoryView.getAdapter();
            innerAdapter.sort();
            innerAdapter.notifyDataSetChanged();
        }
        adapter.notifyDataSetChanged();
    }

    public void addLauncherItem(LauncherItem item) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        final String categoryName = item.getCategory();
        item.setCategoryNoDirty(categoryName);
        CategoryView categoryView = adapter.categories.get(categoryName);
        if (categoryView == null) {
            categoryView = new CategoryView(getContext());
            adapter.categories.put(categoryName, categoryView);
        }
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter) categoryView.getAdapter();
        innerAdapter.add(item);
        innerAdapter.sort();
        innerAdapter.notifyDataSetChanged();
        getAdapter().notifyDataSetChanged();
    }

    // returns true if the category's last item was removed
    public boolean removeLauncherItem(LauncherItem item) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        final String categoryName = item.getCategory();
        final CategoryView categoryView = adapter.categories.get(categoryName);
        //assert appsView != null;
        final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter) categoryView.getAdapter();
        if (innerAdapter.getCount() == 1) {
            // remove category from pager, no need to remove the item from category
            adapter.categories.remove(categoryName);
            adapter.notifyDataSetChanged();
            return true;
        } else {
            // remove item from category
            innerAdapter.remove(item);
            innerAdapter.notifyDataSetChanged();
            return false;
        }
    }

    public void moveLauncherItem(LauncherItem item, String categoryName, boolean followItem) {
        final String oldCategoryName = item.getCategory();

        boolean lastRemoved = false;
        if (oldCategoryName != null) {
            lastRemoved = removeLauncherItem(item);
        }

        item.setCategory(categoryName);
        addLauncherItem(item);
        if (followItem && lastRemoved) {
            showCategory(categoryName);
        }
    }


    public interface Owner {
        void onPagerLongpress();
        void onPagerDoubletap();
        void onPagerPinch();
        void onPagerUnpinch();
    }
}
