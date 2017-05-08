package peterfajdiga.fastdraw.views;

import android.app.WallpaperManager;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class LauncherPager extends ViewPager {

    public LauncherPager(Context context) {
        super(context);
    }

    public LauncherPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        wallpaperManager.setWallpaperOffsets(getWindowToken(), (position + offset) / (getAdapter().getCount() - 1), 0.5f);
    }
}
