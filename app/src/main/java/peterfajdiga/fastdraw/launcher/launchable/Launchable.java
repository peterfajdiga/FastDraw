package peterfajdiga.fastdraw.launcher.launchable;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

import peterfajdiga.fastdraw.launcher.LaunchManager;

public interface Launchable {
    void launch(Context context, LaunchManager launchManager, Bundle opts, Rect clipBounds);
}
