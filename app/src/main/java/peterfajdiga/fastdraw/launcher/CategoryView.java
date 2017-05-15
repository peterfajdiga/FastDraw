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

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.dialogs.CreateShortcutDialog;
import peterfajdiga.fastdraw.logic.LauncherItem;

class CategoryView extends GridView {

    private static final int LONG_CLICK_TIME = ViewConfiguration.getLongPressTimeout();
    private static final float LONG_CLICK_MOUSE_MOVE_TOLERANCE = 50;
    protected long interceptTouchTime;
    protected float interceptTouchX;
    protected float interceptTouchY;

    public CategoryView(final Context context) {
        super(context);

        if (CategoryArrayAdapter.APP_ITEM_STYLE == R.layout.app_item) {
            setNumColumns(GridView.AUTO_FIT);
            setColumnWidth(
                context.getResources().getDimensionPixelSize(R.dimen.app_icon_size) +
                context.getResources().getDimensionPixelSize(R.dimen.app_icon_padding) * 2
            );
        }

        setAdapter(new CategoryArrayAdapter(context));

        final MainActivity activity = (MainActivity)context;
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

                activity.startActivity(intent, opts.toBundle());
            }
        });

        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                // show drop zones
                ((MainActivity)context).showDropZones((LauncherItem)getItemAtPosition(pos));

                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                // start drag
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
                if (Build.VERSION.SDK_INT < 24) {
                    view.startDrag(null, shadow, null, 0);
                } else {
                    view.startDragAndDrop(null, shadow, null, 0);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (System.currentTimeMillis() - interceptTouchTime >= LONG_CLICK_TIME) {
            this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            // change wallpaper
            /*((MainActivity)getContext()).openWallpaperPicker();*/

            // Show shortcut dialog
            CreateShortcutDialog dialog = new CreateShortcutDialog();
            dialog.show(((MainActivity)getContext()).getFragmentManager(), "CreateShortcutDialog");

            interceptTouchTime = Long.MAX_VALUE;
        } else if (mouseMoved(event.getX(), event.getY())) {
            interceptTouchTime = Long.MAX_VALUE;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        interceptTouchTime = System.currentTimeMillis();
        interceptTouchX = event.getX();
        interceptTouchY = event.getY();
        return super.onInterceptTouchEvent(event);
    }

    private boolean mouseMoved(float x, float y) {
        boolean movedX = Math.abs(x - interceptTouchX) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        boolean movedY = Math.abs(y - interceptTouchY) > LONG_CLICK_MOUSE_MOVE_TOLERANCE;
        return movedX || movedY;
    }
}
