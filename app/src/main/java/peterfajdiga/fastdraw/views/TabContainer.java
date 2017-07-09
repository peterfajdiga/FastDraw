package peterfajdiga.fastdraw.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.dialogs.RenameCategoryDialog;
import peterfajdiga.fastdraw.listeners.DropZoneCategory;

public class TabContainer extends TabLayout {

    public TabContainer(Context context) {
        this(context, null);
    }

    public TabContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        final Activity activity = (Activity) getContext();
        final ColorStateList iconColors = ContextCompat.getColorStateList(getContext(), R.color.tab);
        final String categoryName = tab.getText().toString();
        final boolean categoryNameOrdered = isCategoryNameOrdered(categoryName);
        final String visibleCategoryName = categoryNameOrdered ? categoryName.substring(ORDERED_CATEGORY_NAME_KEY_LENGTH) : categoryName;
        final Drawable icon = getCategoryIcon(getContext(), visibleCategoryName);

        if (Preferences.showIcons) {
            if (icon != null) {
                // TODO: Setting to disable
                tab.setIcon(icon);
                tab.setText("");
                DrawableCompat.setTintList(DrawableCompat.wrap(icon), iconColors);
            } else if (visibleCategoryName.length() == 1 && Preferences.largeSingle) {
                // TODO: Setting to disable
                SpannableString tabString = new SpannableString(visibleCategoryName);
                tabString.setSpan(new AbsoluteSizeSpan(19, true), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tab.setText(tabString);
            } else if (categoryNameOrdered) {
                // we need to set the visible part only
                tab.setText(visibleCategoryName);
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
                RenameCategoryDialog dialog = new RenameCategoryDialog();
                Bundle args = new Bundle();
                args.putString("categoryName", categoryName);
                dialog.setArguments(args);
                dialog.show(activity.getFragmentManager(), "RenameCategoryDialog");
                return false;
            }
        });
        tabView.setTag(categoryName);
        tabView.setOnDragListener(new DropZoneCategory());
    }

    private static final int ORDERED_CATEGORY_NAME_KEY_LENGTH = 4;
    private boolean isCategoryNameOrdered(final String category) {
        final char[] chars = category.toCharArray();
        return chars.length > ORDERED_CATEGORY_NAME_KEY_LENGTH
                && chars[2] == ':'
                && chars[3] == ':';
    }


    private static Drawable getCategoryIcon(Context context, String categoryName) {
        if (categoryName.equals(context.getString(R.string.default_category))) {
            return context.getDrawable(R.drawable.ic_category_apps);
        }
        if (categoryName.equals(context.getString(R.string.default_shortcut_category))) {
            return context.getDrawable(R.drawable.ic_category_link);
        }
        switch (categoryName) {
            case "ANDROID":
            case "AOSP":
            case "SYSTEM":      return context.getDrawable(R.drawable.ic_category_android);
            case "CASH":
            case "FINANCE":
            case "FINANCES":
            case "MONEY":       return context.getDrawable(R.drawable.ic_category_money);
            case "BOOKS":
            case "LITERATURE":
            case "READ":
            case "READING":     return context.getDrawable(R.drawable.ic_category_book);
            case "TOOLS":       return context.getDrawable(R.drawable.ic_category_build);
            case "PHONE":       return context.getDrawable(R.drawable.ic_category_phone);
            case "PHOTOS":      return context.getDrawable(R.drawable.ic_category_camera_alt);
            case "CAMERA":      return context.getDrawable(R.drawable.ic_category_camera);
            case "TRAVEL":      return context.getDrawable(R.drawable.ic_category_travel);
            case "CHAT":
            case "CHATTING":
            case "COMMUNICATION":
            case "MESSAGING":
            case "SMS":         return context.getDrawable(R.drawable.ic_category_chat);
            case "CLOUD":       return context.getDrawable(R.drawable.ic_category_cloud);
            case "COMPUTER":
            case "PC":          return context.getDrawable(R.drawable.ic_category_computer);
            case "BAD":
            case "JUNK":
            case "RUBBISH":
            case "TRASH":       return context.getDrawable(R.drawable.ic_category_delete);
            case "BUS":
            case "TRANSPORT":   return context.getDrawable(R.drawable.ic_category_bus);
            case "CAR":
            case "CARS":        return context.getDrawable(R.drawable.ic_category_car);
            case "COMPASS":
            case "EXPLORE":
            case "EXPLORATION":
            case "NAVIGATE":
            case "NAVIGATION":  return context.getDrawable(R.drawable.ic_category_explore);
            case "ADDONS":
            case "ADD-ONS":
            case "EXTENSIONS":
            case "PLUGINS":
            case "PLUG-INS":    return context.getDrawable(R.drawable.ic_category_extension);
            case "<3":
            case "FAVES":
            case "FAVORITE":
            case "FAVORITES":
            case "FAVOURITE":
            case "FAVOURITES":
            case "HEART":       return context.getDrawable(R.drawable.ic_category_heart);
            case "FLIGHT":
            case "PLANE":
            case "PLANES":      return context.getDrawable(R.drawable.ic_category_flight);
            case "TRAFFIC":     return context.getDrawable(R.drawable.ic_category_traffic);
            case "BAR":
            case "DRINK":       return context.getDrawable(R.drawable.ic_category_bar);
            case "FOOD":        return context.getDrawable(R.drawable.ic_category_food);
            case "COFFEE":      return context.getDrawable(R.drawable.ic_category_cafe);
            case "FILES":
            case "FILESYSTEM":  return context.getDrawable(R.drawable.ic_category_folder);
            case "GAMES":       return context.getDrawable(R.drawable.ic_category_games);
            case "PEOPLE":
            case "SOCIAL":      return context.getDrawable(R.drawable.ic_category_group);
            case "HOME":
            case "HOUSE":       return context.getDrawable(R.drawable.ic_category_home);
            case "INFO":
            case "INFORMATION": return context.getDrawable(R.drawable.ic_category_info);
            case "EARTH":
            case "GLOBAL":
            case "GLOBE":
            case "INTERNET":
            case "NET":
            case "WEB":
            case "WORLD":       return context.getDrawable(R.drawable.ic_category_language);
            case "BLOCK":
            case "BLOCKED":
            case "LOCK":
            case "LOCKED":
            case "PROHIBITED":  return context.getDrawable(R.drawable.ic_category_lock);
            case "NOTE":
            case "NOTES":
            case "WRITE":
            case "WRITING":     return context.getDrawable(R.drawable.ic_category_edit);
            case "MOVIES":
            case "VIDEO":       return context.getDrawable(R.drawable.ic_category_movie);
            case "AUDIO":
            case "MUSIC":
            case "SOUND":       return context.getDrawable(R.drawable.ic_category_music);
            case "IMAGES":      return context.getDrawable(R.drawable.ic_category_photo);
            case "MEDIA":       return context.getDrawable(R.drawable.ic_category_play);
            case "EDU":
            case "EDUCATION":
            case "KNOWLEDGE":
            case "LEARN":
            case "LEARNING":
            case "SCHOOL":      return context.getDrawable(R.drawable.ic_category_school);
            case "SEARCH":
            case "SEARCHING":   return context.getDrawable(R.drawable.ic_category_search);
            case "SECURITY":    return context.getDrawable(R.drawable.ic_category_security);
            case "CONF":
            case "CONFIG":
            case "CONFIGURATION":
            case "SETTINGS":    return context.getDrawable(R.drawable.ic_category_settings);
            case "SHOP":
            case "SHOPPING":
            case "STORE":       return context.getDrawable(R.drawable.ic_category_shopping);
            case "DEVICE":
            case "SMARTPHONE":  return context.getDrawable(R.drawable.ic_category_smartphone);
            case "*":
            case "STAR":
            case "STARRED":     return context.getDrawable(R.drawable.ic_category_star);
            case "CALENDAR":
            case "PLANNING":    return context.getDrawable(R.drawable.ic_category_calendar);
            case "BUSINESS":
            case "WORK":        return context.getDrawable(R.drawable.ic_category_work);
            default: return null;
        }
    }
}
