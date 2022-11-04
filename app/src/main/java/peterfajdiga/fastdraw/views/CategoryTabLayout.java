package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.tabs.TabLayout;

import peterfajdiga.fastdraw.Categories;
import peterfajdiga.fastdraw.prefs.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.ShadowDrawable;
import peterfajdiga.fastdraw.dialogs.RenameCategoryDialog;
import peterfajdiga.fastdraw.launcher.DropZone;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;

public class CategoryTabLayout extends TabLayout {
    private OnDropListener onDropListener;
    private RenameCategoryDialog.Listener renameCategoryDialogListener;
    @ColorInt private int shadowColor;

    public CategoryTabLayout(Context context) {
        this(context, null);
        initColors();
    }

    public CategoryTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initColors();
    }

    public CategoryTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initColors();
    }

    public void setOnDropListener(final OnDropListener onDropListener) {
        this.onDropListener = onDropListener;
    }

    public void setRenameCategoryDialogListener(final RenameCategoryDialog.Listener renameCategoryDialogListener) {
        this.renameCategoryDialogListener = renameCategoryDialogListener;
    }

    private void initColors() {
        shadowColor = MaterialColors.getColor(this, R.attr.hardShadowColor);
    }

    @Override
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        super.addTab(tab, setSelected);
        setupTab(tab);
    }

    @Override
    public void addTab(@NonNull Tab tab, int position) {
        super.addTab(tab, position);
        setupTab(tab);
    }

    @Override
    public void addTab(@NonNull Tab tab) {
        super.addTab(tab);
        setupTab(tab);
    }

    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        super.addTab(tab, position, setSelected);
        setupTab(tab);
    }

    private void setupTab(@NonNull Tab tab) {
        if (tab.getTag() == Boolean.TRUE) {
            // this tab has already been setup
            return;
        }
        final FragmentActivity activity = (FragmentActivity)getContext();
        final String categoryName = tab.getText().toString();
        final Drawable icon = Categories.getIconDrawable(getContext(), categoryName);

        if (Preferences.showIcons) {
            if (icon != null) {
                final ShadowDrawable iconWithShadow = new ShadowDrawable(icon, shadowColor);
                tab.setIcon(iconWithShadow);
                tab.setText("");
            } else if (categoryName.length() == 1 && Preferences.largeSingle) {
                SpannableString tabString = new SpannableString(categoryName);
                tabString.setSpan(new AbsoluteSizeSpan(19, true), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tab.setText(tabString);
            }
        }
        tab.setTag(Boolean.TRUE); // mark as done

        // TabView
        final ViewGroup tabContainer = (ViewGroup)getChildAt(0);
        final View tabView = tabContainer.getChildAt(tab.getPosition());

        tabView.setOnLongClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            RenameCategoryDialog dialog = new RenameCategoryDialog(
                renameCategoryDialogListener,
                categoryName,
                getContext().getString(R.string.change_category_icon)
            );
            dialog.show(activity.getSupportFragmentManager(), "RenameCategoryDialog");
            return false;
        });

        tabView.setOnDragListener(new DropZone(
            (draggedItem) -> onDropListener.onDrop(draggedItem, categoryName),
            false
        ));
    }

    public interface OnDropListener {
        void onDrop(final LauncherItem draggedItem, final String category);
    }
}
