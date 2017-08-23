package peterfajdiga.fastdraw.categoryorder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Set;

import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.activities.MainActivity;

public class CategoryOrderAdapter extends RecyclerView.Adapter<CategoryOrderAdapter.CategoryViewHolder> implements ReorderHelperListener {

    private String[] categories;
    private boolean changesMade = false;
    private final PrefMap categoryOrderMap;

    public CategoryOrderAdapter(final PrefMap categoryOrderMap) {
        this.categoryOrderMap = categoryOrderMap;
        Set<String> categorySet = categoryOrderMap.getKeys();
        categories = categorySet.toArray(new String[categorySet.size()]);
        Arrays.sort(categories, new CategoryComparator(categoryOrderMap));
    }

    @Override
    public int getItemCount() {
        return categories.length;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        holder.view.setText(categories[position]);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView view;
        CategoryViewHolder(final View itemView) {
            super(itemView);
            view = (TextView)itemView;
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
        for (int i = 0; i < categories.length; i++) {
            categoryOrderMap.putInt(categories[i], i);
        }
        return true;
    }
}
