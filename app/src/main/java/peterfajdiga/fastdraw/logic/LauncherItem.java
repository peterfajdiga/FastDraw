package peterfajdiga.fastdraw.logic;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.views.LauncherPagerAdapter;

public abstract class LauncherItem implements Comparable<LauncherItem> {

    public String name;
    public Drawable icon;

    protected String category;

    private static List<LauncherItem> dirtyItems = new ArrayList<LauncherItem>();

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.name.toString().compareToIgnoreCase(other.name.toString());
    }

    public abstract String getID();

    public abstract Intent getIntent();

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        markAsDirty();
        this.category = category;
    }
    public void setCategory(String category, MainActivity context, boolean followItem) {
        markAsDirty();

        final LauncherPagerAdapter adapter = context.getPagerAdapter();
        boolean lastRemoved = false;
        if (this.category != null) {
            lastRemoved = adapter.removeLauncherItem(this, this.category);
        }

        this.category = category;
        adapter.addLauncherItem(this, category);
        if (followItem && lastRemoved) {
            context.setPagerToCategory(category);
        }
    }
    public void setCategoryNoDirty(String category, MainActivity context) {
        this.category = category;
        context.getPagerAdapter().addLauncherItem(this, category);
    }

    public abstract void persist(Context context);

    public static void saveDirty(Context context) {
        for (LauncherItem item : dirtyItems) {
            item.persist(context);
        }
        dirtyItems.clear();
    }

    protected void markAsDirty() {
        dirtyItems.add(this);
    }
}
