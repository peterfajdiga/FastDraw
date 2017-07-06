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

public class LauncherPagerHeader extends TabLayout {

    public LauncherPagerHeader(Context context) {
        this(context, null);
    }

    public LauncherPagerHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LauncherPagerHeader(Context context, AttributeSet attrs, int defStyleAttr) {
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
        final String category = tab.getText().toString();
        final Drawable icon = getCategoryIcon(getContext(), category);

        if (Preferences.showIcons()) {
            if (icon != null) {
                // TODO: Setting to disable
                tab.setIcon(icon);
                tab.setText("");
                DrawableCompat.setTintList(DrawableCompat.wrap(icon), iconColors);
            } else if (category.length() == 1) {
                // TODO: Setting to disable
                SpannableString tabString = new SpannableString(category);
                tabString.setSpan(new AbsoluteSizeSpan(19, true), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tab.setText(tabString);
            }
        }
        tab.setTag(Boolean.TRUE);  // mark as done


        // TabView
        final ViewGroup tabContainer = (ViewGroup)getChildAt(0);
        final View tabView = tabContainer.getChildAt(tab.getPosition());

        if (Preferences.mainLayoutResource() == R.layout.activity_main_headertop) {
            tabView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.status_bar_height), 0, 0);
        }
        tabView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                RenameCategoryDialog dialog = new RenameCategoryDialog();
                Bundle args = new Bundle();
                args.putString("categoryName", category);
                dialog.setArguments(args);
                dialog.show(activity.getFragmentManager(), "RenameCategoryDialog");
                return false;
            }
        });
        tabView.setTag(category);
        tabView.setOnDragListener(new DropZoneCategory());
    }


    private static Drawable getCategoryIcon(Context context, String categoryName) {
        if (categoryName.equals(context.getString(R.string.default_category))) {
            return context.getDrawable(R.drawable.ic_apps_white_24dp);
        }
        if (categoryName.equals(context.getString(R.string.default_shortcut_category))) {
            return context.getDrawable(R.drawable.ic_link_white_24dp);
        }
        switch (categoryName) {
            case "ANDROID":
            case "AOSP":
            case "SYSTEM":      return context.getDrawable(R.drawable.ic_android_white_24dp);
            case "CASH":
            case "FINANCE":
            case "FINANCES":
            case "MONEY":       return context.getDrawable(R.drawable.ic_attach_money_white_24dp);
            case "BOOKS":
            case "LITERATURE":
            case "READ":
            case "READING":     return context.getDrawable(R.drawable.ic_book_white_24dp);
            case "TOOLS":       return context.getDrawable(R.drawable.ic_build_white_24dp);
            case "PHONE":       return context.getDrawable(R.drawable.ic_call_white_24dp);
            case "PHOTOS":      return context.getDrawable(R.drawable.ic_camera_alt_white_24dp);
            case "CAMERA":      return context.getDrawable(R.drawable.ic_camera_white_24dp);
            case "TRAVEL":      return context.getDrawable(R.drawable.ic_card_travel_white_24dp);
            case "CHAT":
            case "CHATTING":
            case "COMMUNICATION":
            case "MESSAGING":
            case "SMS":         return context.getDrawable(R.drawable.ic_chat_white_24dp);
            case "CLOUD":       return context.getDrawable(R.drawable.ic_cloud_white_24dp);
            case "COMPUTER":
            case "PC":          return context.getDrawable(R.drawable.ic_computer_white_24dp);
            case "BAD":
            case "JUNK":
            case "RUBBISH":
            case "TRASH":       return context.getDrawable(R.drawable.ic_delete_white_24dp);
            case "BUS":
            case "TRANSPORT":   return context.getDrawable(R.drawable.ic_directions_bus_white_24dp);
            case "CAR":
            case "CARS":        return context.getDrawable(R.drawable.ic_directions_car_white_24dp);
            case "COMPASS":
            case "EXPLORE":
            case "EXPLORATION":
            case "NAVIGATE":
            case "NAVIGATION":  return context.getDrawable(R.drawable.ic_explore_white_24dp);
            case "ADDONS":
            case "ADD-ONS":
            case "EXTENSIONS":
            case "PLUGINS":
            case "PLUG-INS":    return context.getDrawable(R.drawable.ic_extension_white_24dp);
            case "<3":
            case "FAVES":
            case "FAVORITE":
            case "FAVORITES":
            case "FAVOURITE":
            case "FAVOURITES":
            case "HEART":       return context.getDrawable(R.drawable.ic_favorite_white_24dp);
            case "FLIGHT":
            case "PLANE":
            case "PLANES":      return context.getDrawable(R.drawable.ic_flight_white_24dp);
            case "TRAFFIC":     return context.getDrawable(R.drawable.ic_traffic_white_24dp);
            case "BAR":
            case "DRINK":       return context.getDrawable(R.drawable.ic_local_bar_white_24dp);
            case "FOOD":        return context.getDrawable(R.drawable.ic_local_dining_white_24dp);
            case "COFFEE":      return context.getDrawable(R.drawable.ic_local_cafe_white_24dp);
            case "FILES":
            case "FILESYSTEM":  return context.getDrawable(R.drawable.ic_folder_white_24dp);
            case "GAMES":       return context.getDrawable(R.drawable.ic_games_white_24dp);
            case "PEOPLE":
            case "SOCIAL":      return context.getDrawable(R.drawable.ic_group_white_24dp);
            case "HOME":
            case "HOUSE":       return context.getDrawable(R.drawable.ic_home_white_24dp);
            case "INFO":
            case "INFORMATION": return context.getDrawable(R.drawable.ic_info_white_24dp);
            case "EARTH":
            case "GLOBAL":
            case "GLOBE":
            case "INTERNET":
            case "NET":
            case "WEB":
            case "WORLD":       return context.getDrawable(R.drawable.ic_language_white_24dp);
            case "BLOCK":
            case "BLOCKED":
            case "LOCK":
            case "LOCKED":
            case "PROHIBITED":  return context.getDrawable(R.drawable.ic_lock_white_24dp);
            case "NOTE":
            case "NOTES":
            case "WRITE":
            case "WRITING":     return context.getDrawable(R.drawable.ic_mode_edit_white_24dp);
            case "MOVIES":      return context.getDrawable(R.drawable.ic_movie_white_24dp);
            case "AUDIO":
            case "MUSIC":
            case "SOUND":       return context.getDrawable(R.drawable.ic_music_note_white_24dp);
            case "IMAGES":      return context.getDrawable(R.drawable.ic_photo_white_24dp);
            case "MEDIA":       return context.getDrawable(R.drawable.ic_play_arrow_white_24dp);
            case "EDU":
            case "EDUCATION":
            case "KNOWLEDGE":
            case "LEARN":
            case "LEARNING":
            case "SCHOOL":      return context.getDrawable(R.drawable.ic_school_white_24dp);
            case "SEARCH":
            case "SEARCHING":   return context.getDrawable(R.drawable.ic_search_white_24dp);
            case "SECURITY":    return context.getDrawable(R.drawable.ic_security_white_24dp);
            case "CONF":
            case "CONFIG":
            case "CONFIGURATION":
            case "SETTINGS":    return context.getDrawable(R.drawable.ic_settings_white_24dp);
            case "SHOP":
            case "SHOPPING":
            case "STORE":       return context.getDrawable(R.drawable.ic_shopping_cart_white_24dp);
            case "DEVICE":
            case "SMARTPHONE":  return context.getDrawable(R.drawable.ic_smartphone_white_24dp);
            case "*":
            case "STAR":
            case "STARRED":     return context.getDrawable(R.drawable.ic_star_white_24dp);
            case "CALENDAR":
            case "PLANNING":    return context.getDrawable(R.drawable.ic_today_white_24dp);
            case "BUSINESS":
            case "WORK":        return context.getDrawable(R.drawable.ic_work_white_24dp);
            default: return null;
        }
    }
}
