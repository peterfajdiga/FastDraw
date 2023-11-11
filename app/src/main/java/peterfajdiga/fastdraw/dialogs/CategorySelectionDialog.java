package peterfajdiga.fastdraw.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import peterfajdiga.fastdraw.Category;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.views.AutoGridLayoutManager;
import peterfajdiga.fastdraw.views.ResourceUtils;

public class CategorySelectionDialog extends DialogFragment {
    private static final String TITLE_KEY = "title";
    private static final float CATEGORY_ITEM_WIDTH_DP = 24;
    private static final float CATEGORY_ITEM_PADDING_DP = 12;

    public static CategorySelectionDialog newInstance(@NonNull final String title) {
        final Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);

        final CategorySelectionDialog dialog = new CategorySelectionDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    @NonNull
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getContext();
        assert context != null;
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        final Bundle arguments = getArguments();
        assert arguments != null;
        final String title = arguments.getString(TITLE_KEY);

        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.AppThemeOverlay_DayNight);
        builder.setTitle(title);

        final int spanWidth = Math.round(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            CATEGORY_ITEM_WIDTH_DP + 2*CATEGORY_ITEM_PADDING_DP,
            dm
        ));
        final RecyclerView categoriesView = new RecyclerView(context);
        categoriesView.setLayoutManager(new AutoGridLayoutManager(context, spanWidth, GridLayoutManager.VERTICAL, false));
        builder.setView(categoriesView);

        builder.setNegativeButton(android.R.string.cancel, null);

        final Dialog d = builder.create();
        categoriesView.setAdapter(new CategoryItemAdapter(categoryName -> {
            onCategorySelected(categoryName);
            d.dismiss();
        }));
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }

    private void onCategorySelected(final String categoryName) {
        final Activity activity = requireActivity();
        if (activity instanceof OnCategorySelectedListener) {
            ((OnCategorySelectedListener)activity).onCategorySelected(this, categoryName);
        } else {
            throw new RuntimeException(activity.getLocalClassName() + " must implement CategorySelectionDialog.Listener");
        }
    }

    private static class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.ItemViewHolder> {
        private final OnCategoryItemClickedListener itemClickListener;

        public CategoryItemAdapter(final OnCategoryItemClickedListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            final Context context = parent.getContext();
            final DisplayMetrics dm = context.getResources().getDisplayMetrics();
            final ImageView view = new ImageView(context);
            view.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.categoryDialogIconColor)));
            view.setBackgroundResource(ResourceUtils.resolveResourceId(context.getTheme(), android.R.attr.selectableItemBackground));
            final int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CATEGORY_ITEM_PADDING_DP, dm));
            view.setPadding(padding, padding, padding, padding);
            return new ItemViewHolder(view, itemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
            final Category categoryEntry = Category.LIST.get(position);
            holder.bind(
                categoryEntry.name,
                categoryEntry.getIconDrawable(holder.view.getContext())
            );
        }

        @Override
        public int getItemCount() {
            return Category.LIST.size();
        }

        private static class ItemViewHolder extends RecyclerView.ViewHolder {
            private final ImageView view;
            private final OnCategoryItemClickedListener clickListener;

            ItemViewHolder(@NonNull final ImageView itemView, OnCategoryItemClickedListener clickListener) {
                super(itemView);
                this.view = itemView;
                this.clickListener = clickListener;
            }

            void bind(final String categoryName, final Drawable drawable) {
                view.setImageDrawable(drawable);
                view.setOnClickListener(v -> clickListener.OnCategoryItemClicked(categoryName));
            }
        }

        private interface OnCategoryItemClickedListener {
            void OnCategoryItemClicked(String categoryName);
        }
    }

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategorySelectionDialog dialog, String categoryName);
    }
}
