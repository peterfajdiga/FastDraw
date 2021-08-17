package peterfajdiga.fastdraw.launcher;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ItemViewHolder> {
    private final Launcher.Owner owner;
    private final LaunchManager launchManager;
    private final SortedList<LauncherItem> items;

    public CategoryAdapter(@NonNull final Launcher.Owner owner, @NonNull final LaunchManager launchManager) {
        this.owner = owner;
        this.launchManager = launchManager;
        this.items = new SortedList<>(LauncherItem.class, new SortedList.Callback<LauncherItem>() {
            @Override
            public int compare(final LauncherItem o1, final LauncherItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public void onChanged(final int position, final int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(final LauncherItem oldItem, final LauncherItem newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(final LauncherItem item1, final LauncherItem item2) {
                return item1.equals(item2);
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

    SortedList<LauncherItem> getItems() {
        return items;
    }

    @Override
    @NonNull
    public ItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
            .inflate(Preferences.appItemResource, parent, false);
        return new CategoryAdapter.ItemViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        final LauncherItem item = items.get(position);

        final ImageView appIcon = holder.view.findViewById(R.id.app_item_icon);
        appIcon.setImageDrawable(item.getIcon());

        final TextView appLabel = holder.view.findViewById(R.id.app_item_name);
        appLabel.setText(item.getLabel());

        holder.view.setOnClickListener(view -> {
            final ActivityOptions opts;
            if (Build.VERSION.SDK_INT >= 23) {
                opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getWidth(), view.getHeight());
            } else {
                opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
            }

            final Context context = holder.view.getContext();
            item.launch(context, launchManager, opts.toBundle(), view.getClipBounds());
        });

        final PointF touchPoint = new PointF();
        holder.view.setOnTouchListener((view, event) -> {
            touchPoint.set(event.getX(), event.getY());
            return false;
        });
        holder.view.setOnLongClickListener(view -> {
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
            owner.onDragStarted(view, item);

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final View view;

        public ItemViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }
}
