package peterfajdiga.fastdraw.views;

import android.util.TypedValue;

import androidx.annotation.NonNull;

public class ResourceUtils {
    private ResourceUtils() {}

    public static int resolveResourceId(@NonNull final android.content.res.Resources.Theme theme, final int resourceId) {
        final TypedValue resourceValue = new TypedValue();
        theme.resolveAttribute(resourceId, resourceValue, true);
        return resourceValue.resourceId;
    }
}
