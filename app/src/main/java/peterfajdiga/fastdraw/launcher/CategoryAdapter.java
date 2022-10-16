package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import peterfajdiga.fastdraw.Postable;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ItemViewHolder> {
    private final LaunchManager launchManager;
    private final Postable dragEndService;
    private final SortedList<DisplayItem> items;

    public CategoryAdapter(@NonNull final LaunchManager launchManager, final Postable dragEndService) {
        this.launchManager = launchManager;
        this.dragEndService = dragEndService;
        this.items = new SortedList<>(DisplayItem.class, new SortedList.Callback<DisplayItem>() {
            @Override
            public int compare(final DisplayItem o1, final DisplayItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public void onChanged(final int position, final int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(final DisplayItem oldItem, final DisplayItem newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(final DisplayItem item1, final DisplayItem item2) {
                return item1.id.equals(item2.id);
            }

            @Override
            public void onInserted(final int position, final int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(final int position, final int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(final int fromPosition, final int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    SortedList<DisplayItem> getItems() {
        return items;
    }

    @Override
    @NonNull
    public ItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(
            Preferences.appsLinearList ? R.layout.app_item_list : R.layout.app_item_grid,
            parent,
            false
        );
        return new CategoryAdapter.ItemViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        holder.bind(items.get(position), launchManager, dragEndService);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView label;
        private final ImageView icon;

        ItemViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.view = itemView;
            this.label = itemView.findViewById(R.id.app_item_name);
            this.icon = itemView.findViewById(R.id.app_item_icon);
        }

        @SuppressLint("ClickableViewAccessibility")
        void bind(final DisplayItem item, final LaunchManager launchManager, final Postable dragEndService) {
            icon.setImageDrawable(item.icon);
            label.setText(item.label);

            view.setOnClickListener(view -> {
                final ActivityOptions opts;
                opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());

                final Context context = view.getContext();

                try {
                    item.launchable.launch(context, launchManager, opts.toBundle(), view.getClipBounds());
                } catch (final Exception e) {
                    Log.e("OreoShortcutLaunchable", "Failed to launch shortcut " + item.id, e);
                    Toast.makeText(context, R.string.error_launch_exception, Toast.LENGTH_LONG).show();
                }
            });

            final PointF touchPoint = new PointF();
            view.setOnTouchListener((view, event) -> {
                touchPoint.set(event.getX(), event.getY());
                return false;
            });
            view.setOnLongClickListener(view -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                final float x = view.getX() + touchPoint.x;
                final float y = view.getY() + touchPoint.y;

                // start drag
                final View.DragShadowBuilder shadow = new OffsetDragShadowBuilder(view, x, y);
                if (view.startDragAndDrop(null, shadow, item.source, 0)) {
                    final Paint silhouettePaint = new Paint();
                    silhouettePaint.setColorFilter(new LightingColorFilter(Color.BLACK, Color.BLACK));
                    view.setLayerType(View.LAYER_TYPE_SOFTWARE, silhouettePaint);
                }

                dragEndService.post(()-> view.setLayerType(View.LAYER_TYPE_NONE, null)); // avoid creating an OnDragListener to prevent stealing ACTION_DROP from android.R.id.content

                return false;
            });
        }
    }
}
