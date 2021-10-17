package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
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

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.displayitem.DisplayItem;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ItemViewHolder> {
    private final Launcher.ItemDragListener itemDragListener;
    private final LaunchManager launchManager;
    private final SortedList<DisplayItem> items;

    public CategoryAdapter(@NonNull final Launcher.ItemDragListener itemDragListener, @NonNull final LaunchManager launchManager) {
        this.itemDragListener = itemDragListener;
        this.launchManager = launchManager;
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
        holder.bind(items.get(position), launchManager, itemDragListener);
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
        void bind(final DisplayItem item, final LaunchManager launchManager, final Launcher.ItemDragListener itemDragListener) {
            icon.setImageDrawable(item.icon);
            label.setText(item.label);

            view.setOnClickListener(view -> {
                final ActivityOptions opts;
                if (Build.VERSION.SDK_INT >= 23) {
                    opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                } else {
                    opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                }

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
                if (Build.VERSION.SDK_INT < 24) {
                    view.startDrag(null, shadow, null, 0);
                } else {
                    view.startDragAndDrop(null, shadow, null, 0);
                }
                itemDragListener.onDragStarted(view, item.source);

                return false;
            });
        }
    }
}
