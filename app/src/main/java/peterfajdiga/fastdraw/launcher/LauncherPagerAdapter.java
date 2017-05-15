package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.TreeMap;

import peterfajdiga.fastdraw.logic.AppItem;
import peterfajdiga.fastdraw.logic.LauncherItem;

class LauncherPagerAdapter extends PagerAdapter {

    Map<String, CategoryView> categories = new TreeMap<>();

    public LauncherPagerAdapter() {}

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
}
