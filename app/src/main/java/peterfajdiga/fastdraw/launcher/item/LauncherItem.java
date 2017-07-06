package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class LauncherItem implements Comparable<LauncherItem> {

    public String name = "Loading...";
    public Drawable icon = null;

    protected String category;

    private static List<LauncherItem> dirtyItems = new ArrayList<LauncherItem>();

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.name.toString().compareToIgnoreCase(other.name.toString());
    }

    public abstract void load();

    public abstract String getID();

    public abstract Intent getIntent();

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        markAsDirty();
        this.category = category;
    }
    public void setCategoryNoDirty(String category) {
        this.category = category;
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
