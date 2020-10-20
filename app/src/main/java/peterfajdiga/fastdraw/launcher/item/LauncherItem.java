package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class LauncherItem implements Comparable<LauncherItem> {

    public String name = "Loading...";
    public Drawable icon = null;

    @Override
    public int compareTo(@NonNull LauncherItem other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    public abstract String getID();

    public abstract Intent getIntent();

    public abstract void persist(Context context);
}
