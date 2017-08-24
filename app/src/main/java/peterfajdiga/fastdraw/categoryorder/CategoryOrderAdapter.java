package peterfajdiga.fastdraw.categoryorder;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.util.Predicate;

import java.util.Arrays;
import java.util.Set;

import peterfajdiga.fastdraw.Common;
import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.activities.MainActivity;

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
        categoryOrderMap.clean(new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return true;
            }
        });
        reloadOrder();
    }

    public void setItemTouchHelper(final ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @Override
    public int getItemCount() {
        return categories.length;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categoryorder_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final Context context = holder.view.getContext();
        final String categoryName = categories[position];

        final TextView itemText = (TextView)holder.view.findViewById(R.id.text);
        itemText.setText(categoryName);

        final ImageView itemIcon = (ImageView)holder.view.findViewById(R.id.icon);
        itemIcon.setImageDrawable(Common.getCategoryIcon(context, categoryName));
        itemIcon.setColorFilter(context.getResources().getColor(R.color.bottomSheetIcon));

        final View itemHandle = holder.view.findViewById(R.id.handle);
        itemHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (itemTouchHelper != null && MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            }
        });
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        ViewGroup view;
        CategoryViewHolder(final View itemView) {
            super(itemView);
            view = (ViewGroup)itemView;
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

    // return true if there were changes
    public boolean persistOrder(final Context context) {
        if (!changesMade) {
            return false;
        }
        final PrefMap categoryOrder = new PrefMap(context, "categoryorder");
        for (int i = 0; i < categories.length; i++) {
            categoryOrderMap.putInt(categories[i], i);
        }
        return true;
    }
}
