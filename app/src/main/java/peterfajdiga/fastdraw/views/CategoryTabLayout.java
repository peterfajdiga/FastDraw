package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import peterfajdiga.fastdraw.Common;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.dialogs.RenameCategoryDialog;
import peterfajdiga.fastdraw.dragdrop.DropZoneCategory;

public class CategoryTabLayout extends TabLayout {

    public CategoryTabLayout(Context context) {
        this(context, null);
    }

    public CategoryTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CategoryTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setupWithViewPager(@Nullable final ViewPager viewPager, final OnDragListener onDragListener) {
        super.setupWithViewPager(viewPager);
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
        //assert getContext() instanceof Activity;
        final FragmentActivity activity = (FragmentActivity) getContext();
        final ColorStateList iconColors = ContextCompat.getColorStateList(getContext(), R.color.tab);
        final String categoryName = tab.getText().toString();
        final Drawable icon = Common.getCategoryIcon(getContext(), categoryName);

        if (Preferences.showIcons) {
            if (icon != null) {
                tab.setIcon(icon);
                tab.setText("");
                DrawableCompat.setTintList(DrawableCompat.wrap(icon), iconColors);
            } else if (categoryName.length() == 1 && Preferences.largeSingle) {
                SpannableString tabString = new SpannableString(categoryName);
                tabString.setSpan(new AbsoluteSizeSpan(19, true), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tab.setText(tabString);
            }
        }
        tab.setTag(Boolean.TRUE);  // mark as done


        // TabView
        final ViewGroup tabContainer = (ViewGroup)getChildAt(0);
        final View tabView = tabContainer.getChildAt(tab.getPosition());

        if (Preferences.mainLayoutResource == R.layout.activity_main_headertop) {
            tabView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.status_bar_height), 0, 0);
        }
        tabView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                RenameCategoryDialog dialog = new RenameCategoryDialog(
                    categoryName,
                    getContext().getString(R.string.rename_category),
                    getContext().getString(R.string.rename)
                );
                dialog.show(activity.getSupportFragmentManager(), "RenameCategoryDialog");
                return false;
            }
        });
        tabView.setTag(categoryName);
        tabView.setOnDragListener(new DropZoneCategory());
    }
}
