package peterfajdiga.fastdraw.launcher;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.GridView;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.listeners.DragStartListener;

class CategoryView extends GridView {

    private static final int LONG_CLICK_TIME = ViewConfiguration.getLongPressTimeout();
    private static final int DOUBLE_CLICK_TIME = ViewConfiguration.getDoubleTapTimeout();
    private static final float LONG_CLICK_MOUSE_MOVE_TOLERANCE = 50;
    protected long interceptTouchTime;
    protected float interceptTouchX;
    protected float interceptTouchY;

    public CategoryView(final Context context) {
        super(context);

        if (Preferences.appItemResource() == R.layout.app_item) {
            setNumColumns(GridView.AUTO_FIT);
            setColumnWidth(
                context.getResources().getDimensionPixelSize(R.dimen.app_icon_size) +
                context.getResources().getDimensionPixelSize(R.dimen.app_icon_padding) * 2
            );
        }
        setStackFromBottom(Preferences.stackFromBottom());

        setAdapter(new CategoryArrayAdapter(context));

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                LauncherItem app = (LauncherItem)getAdapter().getItem(pos);
                Intent intent = app.getIntent();

                // animation
                ActivityOptions opts;
                if (Build.VERSION.SDK_INT >= 23) {
                    opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                } else {
                    opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                }

                context.startActivity(intent, opts.toBundle());
            }
        });

        setOnItemLongClickListener(new DragStartListener());
    }

    private LauncherPager.Owner getOwner() {
        final Context context = getContext();
        final LauncherPager.Owner owner;
        if (context instanceof LauncherPager.Owner) {
            owner = (LauncherPager.Owner)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LauncherPager.Owner");
        }
        return owner;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (System.currentTimeMillis() - interceptTouchTime >= LONG_CLICK_TIME) {
            this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            getOwner().onPagerLongpress();
            interceptTouchTime = Long.MAX_VALUE;
        } else if (mouseMoved(event.getX(), event.getY())) {
            interceptTouchTime = Long.MAX_VALUE;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // get new values
        final long newInterceptTouchTime = System.currentTimeMillis();
        final float newX = event.getX();
        final float newY = event.getY();

        // double click detection
        final long timeSinceLastClick = newInterceptTouchTime - interceptTouchTime;
        if (timeSinceLastClick <= DOUBLE_CLICK_TIME && !mouseMoved(newX, newY)) {
            getOwner().onPagerDoubletap();
        }

        // save new values
        interceptTouchTime = newInterceptTouchTime;
        interceptTouchX = newX;
        interceptTouchY = newY;

        return super.onInterceptTouchEvent(event);
    }

    private boolean mouseMoved(float x, float y) {
        boolean movedX = Math.abs(x - interceptTouchX) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        boolean movedY = Math.abs(y - interceptTouchY) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        return movedX || movedY;
    }
}
