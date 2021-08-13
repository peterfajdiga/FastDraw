package peterfajdiga.fastdraw.launcher;

import android.app.WallpaperManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Loadable;

public class LauncherPager extends ViewPager {
    private PrefMap itemCategoryMap;
    private LaunchManager launchManager;
    private Owner owner;

    public LauncherPager(@NonNull final Context context) {
        super(context);
        init(context);
    }

    public LauncherPager(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull final Context context) {
        setAdapter(new LauncherPagerAdapter(context));
        itemCategoryMap = new PrefMap(context, "categories"); // TODO: pass from outside
    }

    public void setLaunchManager(@NonNull final LaunchManager launchManager) {
        this.launchManager = launchManager;
    }

    public void setOwner(@NonNull final Owner owner) {
        this.owner = owner;
    }

    @Override
    public void onPageScrolled(final int position, final float offset, final int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        wallpaperManager.setWallpaperOffsets(
            getWindowToken(),
            (position + offset) / (getAdapter().getCount() - 1), // don't use getChildCount()
            0.5f
        );
    }

    /**
     * @return true if category exists and was selected
     */
    public boolean showCategory(@NonNull final String categoryName) {
        final String[] categoryNames = getCategoryNames();
        for (int i = 0; i < categoryNames.length; i++) {
            if (categoryNames[i].equals(categoryName)) {
                setCurrentItem(i);
                return true;
            }
        }
        return false;
    }

    public boolean doesCategoryExist(@NonNull final String categoryName) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        return adapter.categories.get(categoryName) != null;
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

    public LauncherItem[] getLauncherItems(@NonNull final String categoryName) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        final Category category = adapter.categories.get(categoryName);
        return category.getItems();
    }

    public void addLauncherItems(@NonNull final String defaultCategory, @NonNull final LauncherItem... items) {
        final Map<String, List<LauncherItem>> itemsByCategory = getLauncherItemsByCategory(defaultCategory, items);
        for (final Map.Entry<String, List<LauncherItem>> entry : itemsByCategory.entrySet()) {
            addLauncherItems(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, List<LauncherItem>> getLauncherItemsByCategory(@NonNull final String defaultCategory, @NonNull final LauncherItem... items) {
        final Map<String, List<LauncherItem>> itemsByCategory = new HashMap<>();
        for (final LauncherItem item : items) {
            final String categoryName = getItemCategory(item, defaultCategory);
            if (Preferences.hideHidden && categoryName.equals("HIDDEN")) {
                continue;
            }

            List<LauncherItem> categoryItems = itemsByCategory.get(categoryName);
            if (categoryItems == null) {
                categoryItems = new ArrayList<>();
                itemsByCategory.put(categoryName, categoryItems);
            }
            categoryItems.add(item);
        }
        return itemsByCategory;
    }

    private void addLauncherItems(@NonNull final String categoryName, @NonNull final List<LauncherItem> items) {
        assert owner != null;

        if (Preferences.hideHidden && categoryName.equals("HIDDEN")) {
            return;
        }

        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        Category category = adapter.categories.get(categoryName);
        if (category == null) {
            category = new Category(getContext(), owner, launchManager);
            adapter.categories.put(categoryName, category);
            adapter.notifyDataSetChanged();
        }

        category.addItem(items.toArray(new LauncherItem[0]));
        if (adapter.firstCategoryLoaded) {
            for (final LauncherItem item : items) {
                if (item instanceof Loadable) {
                    ((Loadable)item).load(getContext());
                }
            }
        }
    }

    private void addLauncherItem(@NonNull final LauncherItem item, @NonNull final String categoryName, final boolean notify) { // TODO: remove or refactor
        assert owner != null;

        if (Preferences.hideHidden && categoryName.equals("HIDDEN")) {
            return;
        }

        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        Category category = adapter.categories.get(categoryName);
        if (category == null) {
            category = new Category(getContext(), owner, launchManager);
            adapter.categories.put(categoryName, category);
            if (notify) {
                adapter.notifyDataSetChanged();
            }
        }

        if (adapter.firstCategoryLoaded && item instanceof Loadable) {
            ((Loadable)item).load(getContext());
        }
        category.addItem(item);
    }

    /**
     * @return true if the category's last item was removed
     */
    public boolean removeLauncherItem(@NonNull final LauncherItem item) {
        final LauncherPagerAdapter adapter = (LauncherPagerAdapter)super.getAdapter();
        final String categoryName = getItemCategory(item); // TODO: handle null categoryName
        final Category category = adapter.categories.get(categoryName);

        removeItemCategory(item);
        if (category.getItemCount() == 1) {
            // remove category from pager, no need to remove the item from category
            adapter.categories.remove(categoryName);
            adapter.notifyDataSetChanged();
            return true;
        } else {
            // remove item from category
            category.removeItem(item);
            return false;
        }
    }

    public void moveLauncherItem(@NonNull final LauncherItem item, @NonNull final String categoryName, final boolean followItem) {
        final String oldCategoryName = getItemCategory(item);

        boolean lastRemoved = false;
        if (oldCategoryName != null) {
            lastRemoved = removeLauncherItem(item);
        }

        setItemCategory(item, categoryName);
        addLauncherItem(item, categoryName, true);
        if (followItem && lastRemoved) {
            showCategory(categoryName);
        }
    }

    @Nullable
    private String getItemCategory(@NonNull final LauncherItem item) {
        return itemCategoryMap.getString(item.getID(), null);
    }

    @NonNull
    private String getItemCategory(@NonNull final LauncherItem item, @NonNull final String defaultCategory) {
        final String storedCategory = getItemCategory(item);
        if (storedCategory == null) {
            setItemCategory(item, defaultCategory);
            return defaultCategory;
        }
        return storedCategory;
    }

    private void setItemCategory(@NonNull final LauncherItem item, @NonNull final String categoryName) {
        itemCategoryMap.putString(item.getID(), categoryName);
    }

    private void removeItemCategory(@NonNull final LauncherItem item) {
        itemCategoryMap.remove(item.getID());
    }

    public interface Owner {
        void onPagerLongpress();
        void onPagerDoubletap();
        void onPagerPinch();
        void onPagerUnpinch();
        void onDragStarted(@NonNull View draggedView, @NonNull LauncherItem draggedItem);
    }
}
