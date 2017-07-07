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
    private static final float LONG_CLICK_MOUSE_MOVE_TOLERANCE = 50;  // TODO: separate for DOUBLE_CLICK
    private static final float PINCH_DISTANCE_TRIGGER_DELTA = 500000;

    protected long interceptTouchTime;
    protected float interceptTouchX;
    protected float interceptTouchY;

    protected float pinchPrevDistance = 0.0f;
    protected float pinchStartDistance = 0.0f;
    protected float unpinchStartDistance = Float.MAX_VALUE;

    public CategoryView(final Context context) {
        super(context);

        if (Preferences.appItemResource == R.layout.app_item) {
            setNumColumns(GridView.AUTO_FIT);
            setColumnWidth(
                context.getResources().getDimensionPixelSize(R.dimen.app_icon_size) +
                context.getResources().getDimensionPixelSize(R.dimen.app_icon_padding) * 2
            );
        }
        setStackFromBottom(Preferences.stackFromBottom);

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

    private float distanceSquared(final MotionEvent event) {
        //assert event.getPointerCount() == 2;
        final float dx = event.getX(0) - event.getX(1);
        final float dy = event.getY(0) - event.getY(1);
        return dx*dx + dy*dy;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // perform pinch on liftoff
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pinchStartDistance - pinchPrevDistance > PINCH_DISTANCE_TRIGGER_DELTA) {
                getOwner().onPagerPinch();
            } else if (pinchPrevDistance - unpinchStartDistance > PINCH_DISTANCE_TRIGGER_DELTA) {
                getOwner().onPagerUnpinch();
            }
            // reset pinch and unpinch
            pinchPrevDistance  = 0.0f;
            pinchStartDistance = 0.0f;
            unpinchStartDistance = Float.MAX_VALUE;
        }

        switch (event.getPointerCount()) {
            case 2: {
                interceptTouchTime = Long.MAX_VALUE;  // disable long click
                final float newDistance = distanceSquared(event);

                // pinch detection
                if (pinchStartDistance == 0.0f) {
                    // start pinch
                    pinchStartDistance = newDistance;
                } else if (newDistance > pinchPrevDistance) {
                    // user unpinched, reset pinch
                    pinchStartDistance = 0.0f;
                }

                // unpinch detection
                if (unpinchStartDistance == Float.MAX_VALUE) {
                    // start pinch
                    unpinchStartDistance = newDistance;
                } else if (newDistance < pinchPrevDistance) {
                    // user pinched, reset unpinch
                    unpinchStartDistance = Float.MAX_VALUE;
                }

                // update pinchPrevDistance
                pinchPrevDistance = newDistance;
                break;
            }
            case 1: {
                // long click detection
                if (System.currentTimeMillis() - interceptTouchTime >= LONG_CLICK_TIME) {
                    this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    getOwner().onPagerLongpress();
                    interceptTouchTime = Long.MAX_VALUE;
                } else if (mouseMoved(event.getX(), event.getY())) {
                    interceptTouchTime = Long.MAX_VALUE;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            // get new values
            final long newInterceptTouchTime = System.currentTimeMillis();
            final float newX = event.getX();
            final float newY = event.getY();

            // double click detection
            final long timeSinceLastClick = newInterceptTouchTime - interceptTouchTime;
            if (timeSinceLastClick > 0 && timeSinceLastClick <= DOUBLE_CLICK_TIME && !mouseMoved(newX, newY)) {
                getOwner().onPagerDoubletap();
            }

            // save new values
            interceptTouchTime = newInterceptTouchTime;
            interceptTouchX = newX;
            interceptTouchY = newY;
        }

        return super.onInterceptTouchEvent(event);
    }

    private boolean mouseMoved(float x, float y) {
        boolean movedX = Math.abs(x - interceptTouchX) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        boolean movedY = Math.abs(y - interceptTouchY) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        return movedX || movedY;
    }
}
