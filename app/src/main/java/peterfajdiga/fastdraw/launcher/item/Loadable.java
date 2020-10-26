package peterfajdiga.fastdraw.launcher.item;

import android.content.Context;

import androidx.annotation.NonNull;

public interface Loadable {
    void load(@NonNull Context context);
    boolean isLoaded();
}
