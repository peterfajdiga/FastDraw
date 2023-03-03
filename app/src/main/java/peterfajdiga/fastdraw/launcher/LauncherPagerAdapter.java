package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import peterfajdiga.fastdraw.PrefMapCached;
import peterfajdiga.fastdraw.categoryorder.CategoryComparator;

class LauncherPagerAdapter extends PagerAdapter {
    final SortedMap<String, Category> categories;

    public LauncherPagerAdapter(final Context context) {
        categories = new ConcurrentSkipListMap<>(new CategoryComparator(new PrefMapCached(context, "categoryorder")));
    }

    @Override
    @NonNull
    public Object instantiateItem(ViewGroup container, int position) {
        final Category category = (Category)categories.values().toArray()[position];
        container.addView(category.getView());
        return category;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        final Category category = (Category)object;
        container.removeView(category.getView());
    }

    @Override
    public int getCount() {
        return categories.keySet().size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        final Category category = (Category)object;
        return view == category.getView();
    }

    @Override
    public String getPageTitle(int position) {
        return (String)categories.keySet().toArray()[position];
    }

    @Override
    public int getItemPosition(final Object object) {
        final Object[] categories = this.categories.values().toArray();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] == object) {
                return i;
            }
        }
        return POSITION_NONE;
    }
}
