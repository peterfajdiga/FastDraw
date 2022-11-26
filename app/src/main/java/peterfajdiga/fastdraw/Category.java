package peterfajdiga.fastdraw;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Category {
    public static final String defaultCategory = "misc";
    public static final String shortcutsCategory = "shortcuts";
    public static final String hiddenCategory = "hidden";

    public final String name;
    @DrawableRes
    public final int iconResourceId;

    private Category(final String name, @DrawableRes final int iconResourceId) {
        this.name = name;
        this.iconResourceId = iconResourceId;
    }

    public String getName() {
        return name;
    }

    @DrawableRes
    public int getIconResourceId() {
        return iconResourceId;
    }

    @NonNull
    public Drawable getIconDrawable(@NonNull final Context context) {
        final Drawable drawable = ContextCompat.getDrawable(context, iconResourceId);
        assert drawable != null;
        return drawable;
    }

    @Nullable
    public static Drawable getIconDrawable(@NonNull final Context context, @NonNull final String categoryName) {
        final Integer categoryIconResId = ICON_RES_MAP.get(categoryName);
        if (categoryIconResId == null) {
            return null;
        }
        return ContextCompat.getDrawable(context, categoryIconResId);
    }

    public static final List<Category> LIST = List.of(
        new Category(defaultCategory,   R.drawable.ic_category_apps),
        new Category(shortcutsCategory, R.drawable.ic_category_link),
        new Category("android",         R.drawable.ic_category_android),
        new Category("money",           R.drawable.ic_category_money),
        new Category("literature",      R.drawable.ic_category_book),
        new Category("tools",           R.drawable.ic_category_build),
        new Category("phone",           R.drawable.ic_category_phone),
        new Category("photos",          R.drawable.ic_category_camera_alt),
        new Category("camera",          R.drawable.ic_category_camera),
        new Category("communication",   R.drawable.ic_category_chat),
        new Category("cloud",           R.drawable.ic_category_cloud),
        new Category("computer",        R.drawable.ic_category_computer),
        new Category("trash",           R.drawable.ic_category_delete),
        new Category("bus",             R.drawable.ic_category_bus),
        new Category("auto",            R.drawable.ic_category_car),
        new Category("navigation",      R.drawable.ic_category_explore),
        new Category("plugins",         R.drawable.ic_category_extension),
        new Category("heart",           R.drawable.ic_category_heart),
        new Category("flight",          R.drawable.ic_category_flight),
        new Category("traffic",         R.drawable.ic_category_traffic),
        new Category("drink",           R.drawable.ic_category_bar),
        new Category("food",            R.drawable.ic_category_food),
        new Category("coffee",          R.drawable.ic_category_cafe),
        new Category("filesystem",      R.drawable.ic_category_folder),
        new Category("games",           R.drawable.ic_category_games),
        new Category("social",          R.drawable.ic_category_group),
        new Category("home",            R.drawable.ic_category_home),
        new Category("info",            R.drawable.ic_category_info),
        new Category("internet",        R.drawable.ic_category_language),
        new Category("locked",          R.drawable.ic_category_lock),
        new Category("notes",           R.drawable.ic_category_edit),
        new Category("video",           R.drawable.ic_category_movie),
        new Category("audio",           R.drawable.ic_category_music),
        new Category("images",          R.drawable.ic_category_photo),
        new Category("media",           R.drawable.ic_category_play),
        new Category("education",       R.drawable.ic_category_school),
        new Category("search",          R.drawable.ic_category_search),
        new Category("security",        R.drawable.ic_category_security),
        new Category("configuration",   R.drawable.ic_category_settings),
        new Category("shopping",        R.drawable.ic_category_shopping),
        new Category("smartphone",      R.drawable.ic_category_smartphone),
        new Category("starred",         R.drawable.ic_category_star),
        new Category("calendar",        R.drawable.ic_category_calendar),
        new Category("work",            R.drawable.ic_category_work)
    );

    private static final Map<String, Integer> ICON_RES_MAP = LIST.stream().collect(Collectors.toMap(Category::getName, Category::getIconResourceId));

    public static String oldToNewCategoryName(final String oldCategoryName) {
        switch (oldCategoryName) {
            case "MISC":
            case "UNMOVED":     return defaultCategory;
            case "SHORTCUTS":   return shortcutsCategory;
            case "HIDDEN":      return hiddenCategory;
            case "ANDROID":
            case "AOSP":
            case "DEVEL":
            case "DEVELOPMENT":
            case "SYSTEM":      return "android";
            case "CASH":
            case "FINANCE":
            case "FINANCES":
            case "MONEY":       return "money";
            case "BOOKS":
            case "LITERATURE":
            case "READ":
            case "READING":     return "literature";
            case "TOOLS":       return "tools";
            case "PHONE":       return "phone";
            case "PHOTOS":      return "photos";
            case "CAMERA":      return "camera";
            case "CHAT":
            case "CHATTING":
            case "COMMUNICATION":
            case "MESSAGING":
            case "SMS":         return "communication";
            case "CLOUD":       return "cloud";
            case "COMPUTER":
            case "PC":          return "computer";
            case "GARBAGE":
            case "JUNK":
            case "RUBBISH":
            case "TRASH":       return "trash";
            case "BUS":
            case "TRANSPORT":   return "bus";
            case "AUTO":
            case "CAR":
            case "CARS":        return "auto";
            case "COMPASS":
            case "EXPLORE":
            case "EXPLORATION":
            case "MAPS":
            case "NAVIGATE":
            case "NAVIGATION":  return "navigation";
            case "ADDONS":
            case "ADD-ONS":
            case "EXTENSIONS":
            case "PLUGINS":
            case "PLUG-INS":    return "plugins";
            case "<3":
            case "FAVES":
            case "FAVORITE":
            case "FAVORITES":
            case "FAVOURITE":
            case "FAVOURITES":
            case "HEART":       return "heart";
            case "FLIGHT":
            case "PLANE":
            case "PLANES":      return "flight";
            case "TRAFFIC":     return "traffic";
            case "BAR":
            case "DRINK":       return "drink";
            case "FOOD":        return "food";
            case "COFFEE":      return "coffee";
            case "FILES":
            case "FILESYSTEM":  return "filesystem";
            case "GAMES":       return "games";
            case "CONTACTS":
            case "PEOPLE":
            case "SOCIAL":      return "social";
            case "HOME":
            case "HOUSE":       return "home";
            case "INFO":
            case "INFORMATION": return "info";
            case "EARTH":
            case "GLOBAL":
            case "GLOBE":
            case "INTERNET":
            case "NET":
            case "WEB":
            case "WORLD":       return "internet";
            case "BLOCK":
            case "BLOCKED":
            case "LOCK":
            case "LOCKED":
            case "PROHIBITED":  return "locked";
            case "NOTE":
            case "NOTES":
            case "WRITE":
            case "WRITING":     return "notes";
            case "MOVIES":
            case "VIDEO":       return "video";
            case "AUDIO":
            case "MUSIC":
            case "SOUND":       return "audio";
            case "IMAGES":      return "images";
            case "MEDIA":       return "media";
            case "EDU":
            case "EDUCATION":
            case "KNOWLEDGE":
            case "LEARN":
            case "LEARNING":
            case "SCHOOL":      return "education";
            case "SEARCH":
            case "SEARCHING":   return "search";
            case "SECURITY":    return "security";
            case "CONF":
            case "CONFIG":
            case "CONFIGURATION":
            case "SETTINGS":    return "configuration";
            case "SHOP":
            case "SHOPPING":
            case "STORE":       return "shopping";
            case "DEVICE":
            case "SMARTPHONE":  return "smartphone";
            case "*":
            case "STAR":
            case "STARRED":     return "starred";
            case "CALENDAR":
            case "PLANNING":    return "calendar";
            case "BUSINESS":
            case "WORK":        return "work";
            default: return oldCategoryName;
        }
    }
}
