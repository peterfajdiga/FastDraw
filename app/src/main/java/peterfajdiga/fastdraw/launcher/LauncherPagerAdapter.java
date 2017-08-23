package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.categoryorder.CategoryComparator;

class LauncherPagerAdapter extends PagerAdapter {

    final SortedMap<String, CategoryView> categories;
    boolean firstCategoryLoaded = false;

    public LauncherPagerAdapter(final Context context) {
        categories = new ConcurrentSkipListMap<>(new CategoryComparator(new PrefMap(context, "categoryorder")));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final CategoryView layout = (CategoryView)categories.values().toArray()[position];
        container.addView(layout);
        if (!firstCategoryLoaded) {
            loadCategories(layout);
            firstCategoryLoaded = true;
        }
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


    /* loading */

    public void loadCategories(final CategoryView firstToLoad) {
        // load first category
        final CategoryArrayAdapter firstInnerAdapter = (CategoryArrayAdapter)firstToLoad.getAdapter();
        firstInnerAdapter.loadItems();

        // load all others
        for (CategoryView categoryView : categories.values()) {
            if (categoryView == firstToLoad) {
                // already loaded
                continue;
            }
            final CategoryArrayAdapter innerAdapter = (CategoryArrayAdapter)categoryView.getAdapter();
            innerAdapter.loadItemsAsync();
        }
    }
}
