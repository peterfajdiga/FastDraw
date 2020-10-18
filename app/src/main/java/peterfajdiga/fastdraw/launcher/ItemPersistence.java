package peterfajdiga.fastdraw.launcher;

import android.content.Context;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.item.LauncherItem;

public class ItemPersistence {
    private ItemPersistence() {}

    public static void persistItem(@NonNull final Context context, @NonNull final LauncherItem item, @NonNull final String category) {
        item.persist(context);
    }
}
