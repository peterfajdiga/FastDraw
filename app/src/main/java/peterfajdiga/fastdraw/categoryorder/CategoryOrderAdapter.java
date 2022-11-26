package peterfajdiga.fastdraw.categoryorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Set;

import peterfajdiga.fastdraw.Category;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;
import peterfajdiga.fastdraw.prefs.PrefMap;

public class CategoryOrderAdapter extends RecyclerView.Adapter<CategoryOrderAdapter.CategoryViewHolder> implements ReorderHelperListener {
    private String[] categories;
    private boolean changesMade = false;
    private final PrefMap categoryOrderMap;
    private ItemTouchHelper itemTouchHelper = null;

    public CategoryOrderAdapter(final PrefMap categoryOrderMap) {
        this.categoryOrderMap = categoryOrderMap;
        reloadCategories();
    }

    public void reloadCategories() {
        Set<String> categorySet = categoryOrderMap.getKeys();
        categories = categorySet.toArray(new String[categorySet.size()]);
        reloadOrder();
    }

    public void reloadOrder() {
        Arrays.sort(categories, new CategoryComparator(categoryOrderMap));
        notifyDataSetChanged();
    }

    public void orderReset() {
        changesMade = false;
        MainActivity.forceFinish();
        for (String category : categories) {
            categoryOrderMap.putInt(category, CategoryComparator.UNORDERED);
        }
        reloadOrder();
    }

    public void setItemTouchHelper(final ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @Override
    public int getItemCount() {
        return categories.length;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.categoryorder_item, parent, false);
        return new CategoryViewHolder(view, itemTouchHelper);
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        holder.bind(categories[position]);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final ImageView icon;

        @SuppressLint("ClickableViewAccessibility")
        CategoryViewHolder(final View itemView, final ItemTouchHelper itemTouchHelper) {
            super(itemView);
            label = itemView.findViewById(R.id.text);
            icon = itemView.findViewById(R.id.icon);
            final View handle = itemView.findViewById(R.id.handle);
            handle.setOnTouchListener((v, event) -> {
                if (itemTouchHelper != null && MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this);
                }
                return false;
            });
        }

        void bind(final String categoryName) {
            this.label.setText(categoryName);
            this.icon.setImageDrawable(Category.getIconDrawable(this.icon.getContext(), categoryName));
        }
    }

    @Override
    public boolean onItemMove(final int position_source, final int position_dest) {
        if (position_source < position_dest) {
            for (int i = position_source; i < position_dest; i++) {
                final String tmp = categories[i];
                categories[i] = categories[i+1];
                categories[i+1] = tmp;
            }
        } else {
            for (int i = position_source; i > position_dest; i--) {
                final String tmp = categories[i];
                categories[i] = categories[i-1];
                categories[i-1] = tmp;
            }
        }
        notifyItemMoved(position_source, position_dest);
        changesMade = true;
        MainActivity.forceFinish();
        return true;
    }

    /**
     * Save the changes
     *
     * @return true if there were changes
     */
    public boolean persistOrder(final Context context) {
        if (!changesMade) {
            return false;
        }
        for (int i = 0; i < categories.length; i++) {
            categoryOrderMap.putInt(categories[i], i);
        }
        return true;
    }
}
