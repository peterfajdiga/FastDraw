package peterfajdiga.fastdraw.launcher;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Loadable;

public class Launcher {
    private final PrefMap itemCategoryMap;
    private final LaunchManager launchManager;
    private final Owner owner;
    private final ViewPager pager;
    private final LauncherPagerAdapter adapter;

    public Launcher(
        @NonNull final LaunchManager launchManager,
        @NonNull final Owner owner,
        @NonNull final ViewPager pager
    ) {
        final Context context = pager.getContext();
        this.itemCategoryMap = new PrefMap(context, "categories"); // TODO: pass from outside
        this.launchManager = launchManager;
        this.owner = owner;
        this.pager = pager;
        this.adapter = new LauncherPagerAdapter(context);
        pager.setAdapter(this.adapter);
    }

    /**
     * @return true if category exists and was selected
     */
    public boolean showCategory(@NonNull final String categoryName) {
        final String[] categoryNames = getCategoryNames();
        for (int i = 0; i < categoryNames.length; i++) {
            if (categoryNames[i].equals(categoryName)) {
                pager.setCurrentItem(i);
                return true;
            }
        }
        return false;
    }

    public void showFirstCategory() {
        pager.setCurrentItem(0);
    }

    public boolean doesCategoryExist(@NonNull final String categoryName) {
        return adapter.categories.get(categoryName) != null;
    }

    public String getCurrentCategoryName() {
        return adapter.getPageTitle(pager.getCurrentItem());
    }

    public String[] getCategoryNames() {
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
            addItemsToCategory(entry.getKey(), entry.getValue().toArray(new LauncherItem[0]));
        }
    }

    private void addItemsToCategory(@NonNull final String categoryName, @NonNull final LauncherItem... items) {
        if (Preferences.hideHidden && categoryName.equals("HIDDEN")) {
            return;
        }

        Category category = adapter.categories.get(categoryName);
        if (category == null) {
            final Context context = pager.getContext();
            category = new Category(context, owner, launchManager);
            adapter.categories.put(categoryName, category);
            adapter.notifyDataSetChanged();
        }

        addItemsToCategory(category, items);
    }

    private void addItemsToCategory(@NonNull final Category category, @NonNull final LauncherItem... items) {
        loadAndAddItems(category, items);
    }

    private void loadAndAddItems(@NonNull final Category category, @NonNull final LauncherItem... items) {
        final Context context = pager.getContext();
        final Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            loadItems(context, items);
            handler.post(() -> category.addItems(items));
        });
    }

    private static void loadItems(final Context context, @NonNull final LauncherItem... items) {
        for (final LauncherItem item : items) {
            if (item instanceof Loadable) {
                try {
                    ((Loadable)item).load(context);
                } catch (final Exception e) {
                    Log.e("Category", "Failed to load item of package " + item.getPackageName(), e);
                }
            }
        }
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
            category.removeItem(item);
            return false;
        }
    }

    public void moveItem(@NonNull final LauncherItem item, @NonNull final String categoryName, final boolean followItem) {
        final String oldCategoryName = getItemCategory(item);

        boolean lastRemoved = false;
        if (oldCategoryName != null) {
            lastRemoved = removeItem(item, false);
        }

        setItemCategory(item, categoryName);
        addItemsToCategory(categoryName, item);
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
        void onLongpress();
        void onDoubletap();
        void onPinch();
        void onUnpinch();
        void onDragStarted(@NonNull View draggedView, @NonNull LauncherItem draggedItem);
    }
}
