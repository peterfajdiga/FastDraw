package peterfajdiga.fastdraw.views;

import android.app.WallpaperManager;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class WallPager extends ViewPager {
    public WallPager(@NonNull final Context context) {
        super(context);
    }

    public WallPager(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPageScrolled(final int position, final float offset, final int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);

        final PagerAdapter adapter = getAdapter();
        final float xOffset;
        if (adapter == null || adapter.getCount() <= 1) {
            xOffset = 0.5f;
        } else {
            xOffset = (position + offset) / (getAdapter().getCount() - 1.0f);
        }

        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        wallpaperManager.setWallpaperOffsets(getWindowToken(), xOffset, 0.5f);
    }
}
