package peterfajdiga.fastdraw.launcher;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

class CategoryView extends GridView {

    private static final int LONG_CLICK_TIME = ViewConfiguration.getLongPressTimeout();
    private static final int DOUBLE_CLICK_TIME = ViewConfiguration.getDoubleTapTimeout();
    private static final float LONG_CLICK_MOUSE_MOVE_TOLERANCE = 50; // TODO: separate for DOUBLE_CLICK
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

                if (intent == null) {
                    final String errMessage = context.getString(R.string.no_intent);
                    final Toast toast = Toast.makeText(context, errMessage, Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                // animation
                ActivityOptions opts;
                if (Build.VERSION.SDK_INT >= 23) {
                    opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                } else {
                    opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                }

                switch (intent.getAction()) {
                    case Intent.ACTION_CALL: launchWithPermission(intent, opts, Manifest.permission.CALL_PHONE); break;
                    default: try {
                        context.startActivity(intent, opts.toBundle());
                    } catch (ActivityNotFoundException | IllegalArgumentException e) {
                        final String errMessage = context.getString(R.string.no_app);
                        final Toast toast = Toast.makeText(context, errMessage, Toast.LENGTH_LONG);
                        toast.show();
                    }
                    break;
                }
            }
        });

        final LauncherPager.Owner owner = getOwner();
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                // start drag
                final View.DragShadowBuilder shadow = new OffsetDragShadowBuilder(view, interceptTouchX, interceptTouchY);
                if (Build.VERSION.SDK_INT < 24) {
                    view.startDrag(null, shadow, null, 0);
                } else {
                    view.startDragAndDrop(null, shadow, null, 0);
                }
                owner.onDragStarted(view, (LauncherItem)parent.getItemAtPosition(position));

                return false;
            }
        });
    }

    private void launchWithPermission(final Intent launchIntent, final ActivityOptions launchOpts, final String permission) {
        final LauncherPager.Owner owner = getOwner();
        //assert owner instanceof Activity;
        ActivityCompat.requestPermissions((Activity)owner, new String[]{permission}, LauncherPager.LAUNCH_PERMISSION);
        owner.setDelayedLaunchIntent(launchIntent, launchOpts.toBundle());
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
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // perform pinch on liftoff
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
                interceptTouchTime = Long.MAX_VALUE; // disable long click
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
                } else if (hasPointerMoved(event.getX(), event.getY())) {
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
            if (timeSinceLastClick > 0 && timeSinceLastClick <= DOUBLE_CLICK_TIME && !hasPointerMoved(newX, newY)) {
                getOwner().onPagerDoubletap();
            }

            // save new values
            interceptTouchTime = newInterceptTouchTime;
            interceptTouchX = newX;
            interceptTouchY = newY;
        }

        return super.onInterceptTouchEvent(event);
    }

    private boolean hasPointerMoved(float x, float y) {
        boolean movedX = Math.abs(x - interceptTouchX) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        boolean movedY = Math.abs(y - interceptTouchY) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        return movedX || movedY;
    }

    private float distanceSquared(final MotionEvent event) {
        //assert event.getPointerCount() == 2;
        final float dx = event.getX(0) - event.getX(1);
        final float dy = event.getY(0) - event.getY(1);
        return dx*dx + dy*dy;
    }
}
