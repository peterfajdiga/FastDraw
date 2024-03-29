package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.tabs.TabLayout;

import peterfajdiga.fastdraw.Category;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.ShadowDrawable;
import peterfajdiga.fastdraw.launcher.DropZone;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;

public class CategoryTabLayout extends TabLayout {
    private OnDropListener onDropListener;
    private OnTabLongClickListener onTabLongClickListener;
    @ColorInt private int shadowColor;
    private PointF shadowOffset;

    public CategoryTabLayout(Context context) {
        this(context, null);
        init();
    }

    public CategoryTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public CategoryTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnDropListener(final OnDropListener onDropListener) {
        this.onDropListener = onDropListener;
    }

    public void setOnTabLongClickListener(final OnTabLongClickListener onTabLongClickListener) {
        this.onTabLongClickListener = onTabLongClickListener;
    }

    private void init() {
        final Resources res = getResources();
        shadowColor = MaterialColors.getColor(this, R.attr.hardShadowColor);
        shadowOffset = new PointF(
            res.getDimension(R.dimen.tab_icon_shadow_offset_x),
            res.getDimension(R.dimen.tab_icon_shadow_offset_y)
        );
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
        final String categoryName = tab.getText().toString();
        final Drawable icon = Category.getIconDrawable(getContext(), categoryName);

        if (icon != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                final ShadowDrawable iconWithShadow = new ShadowDrawable(icon, shadowColor, shadowOffset);
                tab.setIcon(iconWithShadow);
            } else {
                tab.setIcon(icon);
            }
            tab.setText("");
        }
        tab.setTag(Boolean.TRUE); // mark as done

        // TabView
        final ViewGroup tabContainer = (ViewGroup)getChildAt(0);
        final View tabView = tabContainer.getChildAt(tab.getPosition());

        tabView.setOnLongClickListener(view -> onTabLongClickListener.onLongClick(categoryName));

        tabView.setOnDragListener(new DropZone(
            (draggedItem) -> onDropListener.onDrop(draggedItem, categoryName),
            false
        ));
    }

    public interface OnDropListener {
        void onDrop(final LauncherItem draggedItem, final String categoryName);
    }

    public interface OnTabLongClickListener {
        boolean onLongClick(final String categoryName);
    }
}
