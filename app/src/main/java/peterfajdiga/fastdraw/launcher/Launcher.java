package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import peterfajdiga.fastdraw.Postable;
import peterfajdiga.fastdraw.categoryorder.CategoryComparator;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;
import peterfajdiga.fastdraw.prefs.PrefMap;
import peterfajdiga.fastdraw.prefs.Preferences;
import peterfajdiga.fastdraw.views.NestedScrollChildManager;

public class Launcher {
    public static final String HOME_CATEGORY_NAME = "HOME";

    private final PrefMap itemCategoryMap;
    private final PrefMap categoriesOrderMap;
    private final LaunchManager launchManager;
    private final Postable dragEndService;
    private final View.OnTouchListener backgroundTouchListener;
    private final ViewPager pager;
    private final LauncherPagerAdapter adapter;

    public Launcher(
        @NonNull final LaunchManager launchManager,
        @NonNull final Postable dragEndService,
        @NonNull final View.OnTouchListener backgroundTouchListener,
        @NonNull final ViewPager pager
    ) {
        final Context context = pager.getContext();
        this.itemCategoryMap = new PrefMap(context, "categories"); // TODO: pass from outside
        this.categoriesOrderMap = new PrefMap(context, "categoryorder"); // TODO: pass from outside
        this.launchManager = launchManager;
        this.dragEndService = dragEndService;
        this.backgroundTouchListener = backgroundTouchListener;
        this.pager = pager;
        this.adapter = new LauncherPagerAdapter(context);
        pager.setAdapter(this.adapter);
    }

    public void showCategory(@NonNull final String categoryName) {
        final String[] categoryNames = getCategoryNames();
        for (int i = 0; i < categoryNames.length; i++) {
            if (categoryNames[i].equals(categoryName)) {
                pager.setCurrentItem(i);
                return;
            }
        }
        Log.w("Launcher", "Trying to show nonexistent category " + categoryName);
    }

    public void showInitialCategory() {
        showCategory(getInitialCategory());
    }

    public boolean doesCategoryExist(@NonNull final String categoryName) {
        return adapter.categories.get(categoryName) != null;
    }

    public String getCurrentCategoryName() {
        return adapter.getPageTitle(pager.getCurrentItem());
    }

    @Nullable
    public NestedScrollChildManager getScrollChildManager() {
        final Category currentCategory = adapter.categories.get(getCurrentCategoryName());
        if (currentCategory == null) {
            return null;
        }
        return currentCategory.nestedScrollChildManager;
    }

    public void smoothScrollUpCurrent() {
        final View categoryView = adapter.categories.values().toArray(new Category[0])[pager.getCurrentItem()].getView();
        if (categoryView instanceof RecyclerView) {
            ((RecyclerView)categoryView).smoothScrollToPosition(0);
        } else {
            categoryView.scrollTo(0, 0);
        }
    }

    public void scrollUpAllExceptCurrent() {
        final int currentItemPosition = pager.getCurrentItem();
        int i = 0;
        for (final Category category : adapter.categories.values()) {
            if (i != currentItemPosition) {
                final View categoryView = category.getView();
                if (categoryView instanceof RecyclerView) {
                    ((RecyclerView)categoryView).scrollToPosition(0);
                } else {
                    categoryView.scrollTo(0, 0);
                }
            }
            i++;
        }
    }

    private String[] getCategoryNames() {
        final Object[] names = adapter.categories.keySet().toArray();
        String[] retval = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            retval[i] = (String)names[i];
        }
        return retval;
    }

    public LauncherItem[] getItems(@NonNull final String categoryName) {
        final Category category = adapter.categories.get(categoryName);
        return category.getItems();
    }

    public List<LauncherItem> getItems() {
        final List<LauncherItem> launcherItems = new ArrayList<>();
        for (final Category category : adapter.categories.values()) {
            launcherItems.addAll(Arrays.asList(category.getItems()));
        }
        return launcherItems;
    }

    public void addItems(@NonNull final String defaultCategory, @NonNull final LauncherItem... items) {
        final Map<String, List<LauncherItem>> itemsByCategory = categorizeItems(defaultCategory, items);
        for (final Map.Entry<String, List<LauncherItem>> entry : itemsByCategory.entrySet()) {
            addItemsToCategory(entry.getKey(), false, entry.getValue().toArray(new LauncherItem[0]));
        }
    }

    public void addItemsStartup(@NonNull final String defaultCategory, @NonNull final LauncherItem... items) {
        final Map<String, List<LauncherItem>> itemsByCategory = categorizeItems(defaultCategory, items);
        createCategories(itemsByCategory.keySet());
        final String initialCategoryName = getInitialCategory();
        for (final Map.Entry<String, List<LauncherItem>> entry : itemsByCategory.entrySet()) {
            final String categoryName = entry.getKey();
            addItemsToCategory(
                entry.getKey(),
                categoryName.equals(initialCategoryName),
                entry.getValue().toArray(new LauncherItem[0])
            );
        }
    }

    private void addItemsToCategory(@NonNull final String categoryName, final boolean immediate, @NonNull final LauncherItem... items) {
        if (Preferences.hideHidden && categoryName.equals("HIDDEN")) {
            return;
        }

        Category category = adapter.categories.get(categoryName);
        if (category == null) {
            final Context context = pager.getContext();
            category = new Category(context, launchManager, dragEndService, backgroundTouchListener);
            categoriesOrderMap.getIntCreate(categoryName, CategoryComparator.UNORDERED); // make sure the new category is added to the categoryorder prefs
            adapter.categories.put(categoryName, category);
            adapter.notifyDataSetChanged();
        }

        loadAndAddItems(category, immediate, items);
    }

    private void loadAndAddItems(@NonNull final Category category, final boolean immediate, @NonNull final LauncherItem... items) {
        if (immediate) {
            category.addItems(createDisplayItems(items));
        } else {
            final Handler handler = new Handler(Looper.getMainLooper());
            Executors.newSingleThreadExecutor().execute(() -> {
                final DisplayItem[] displayItems = createDisplayItems(items);
                handler.post(() -> category.addItems(displayItems));
            });
        }
    }

    private DisplayItem[] createDisplayItems(@NonNull final LauncherItem... items) {
        final DisplayItem[] displayItems = new DisplayItem[items.length];
        for (int i = 0; i < items.length; i++) {
            displayItems[i] = items[i].getDisplayItem(pager.getContext());
        }
        return displayItems;
    }

    private Map<String, List<LauncherItem>> categorizeItems(@NonNull final String defaultCategory, @NonNull final LauncherItem... items) {
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

    private void createCategories(@NonNull final Set<String> categories) {
        for (final String categoryName : categories) {
            if (adapter.categories.containsKey(categoryName)) {
                continue;
            }
            final Category category = new Category(pager.getContext(), launchManager, dragEndService, backgroundTouchListener);
            adapter.categories.put(categoryName, category);
        }
        adapter.notifyDataSetChanged();
    }

    private String getInitialCategory() {
        final Category homeCategory = adapter.categories.get(HOME_CATEGORY_NAME);
        if (homeCategory != null) {
            return HOME_CATEGORY_NAME;
        } else {
            return adapter.categories.firstKey();
        }
    }

    /**
     * @return true if the category's last item was removed
     */
    public boolean removeItem(@NonNull final LauncherItem item, final boolean permanent) {
        final String categoryName = getItemCategory(item); // TODO: handle null categoryName
        final Category category = adapter.categories.get(categoryName);

        if (permanent) {
            removeItemCategory(item);
        }
        if (category.getItemCount() == 1) {
            // remove category from pager, no need to remove the item from category
            adapter.categories.remove(categoryName);
            adapter.notifyDataSetChanged();
            return true;
        } else {
            // remove item from category
            category.removeItem(item.getId());
            return false;
        }
    }

    public void updateItem(@NonNull final LauncherItem existingItem, @NonNull final LauncherItem updatedItem) {
        final String categoryName = getItemCategory(existingItem);
        final Category category = adapter.categories.get(categoryName);
        category.updateItem(existingItem.getId(), updatedItem.getDisplayItem(pager.getContext()));
    }

    public void moveItems(@NonNull final String categoryName, @NonNull final LauncherItem... items) {
        final String currentCategoryName = getCurrentCategoryName();
        boolean followItem = false;

        for (final LauncherItem item : items) {
            final String oldCategoryName = getItemCategory(item);
            if (oldCategoryName != null) {
                final boolean lastItemRemoved = removeItem(item, false);
                followItem |= lastItemRemoved && oldCategoryName.equals(currentCategoryName);
            }

            setItemCategory(item, categoryName);
        }

        addItemsToCategory(categoryName, false, items);
        if (followItem) {
            showCategory(categoryName);
        }
    }

    public void moveCategory(@NonNull final String oldCategoryName, @NonNull final String newCategoryName) {
        final boolean follow = oldCategoryName.equals(getCurrentCategoryName());
        final LauncherItem[] items = getItems(oldCategoryName);

        adapter.categories.remove(oldCategoryName);
        adapter.notifyDataSetChanged();

        for (final LauncherItem item : items) {
            setItemCategory(item, newCategoryName);
        }

        addItemsToCategory(newCategoryName, false, items);
        if (follow) {
            showCategory(newCategoryName);
        }
    }

    @Nullable
    private String getItemCategory(@NonNull final LauncherItem item) {
        return itemCategoryMap.getString(item.getId(), null);
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
        itemCategoryMap.putString(item.getId(), categoryName);
    }

    private void removeItemCategory(@NonNull final LauncherItem item) {
        itemCategoryMap.remove(item.getId());
    }
}
