package peterfajdiga.fastdraw;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Map;

public class Categories {
    private Categories() {}

    public static Drawable getIconDrawable(final Context context, @NonNull final String categoryName) {
        final Integer categoryIconResId = MAP.get(categoryName);
        if (categoryIconResId == null) {
            return null;
        }
        return ContextCompat.getDrawable(context, categoryIconResId);
    }

    public static final Map<String, Integer> MAP = Map.ofEntries(
        Map.entry("misc",          R.drawable.ic_category_apps),
        Map.entry("shortcuts",     R.drawable.ic_category_link),
        Map.entry("android",       R.drawable.ic_category_android),
        Map.entry("money",         R.drawable.ic_category_money),
        Map.entry("literature",    R.drawable.ic_category_book),
        Map.entry("tools",         R.drawable.ic_category_build),
        Map.entry("phone",         R.drawable.ic_category_phone),
        Map.entry("photos",        R.drawable.ic_category_camera_alt),
        Map.entry("camera",        R.drawable.ic_category_camera),
        Map.entry("communication", R.drawable.ic_category_chat),
        Map.entry("cloud",         R.drawable.ic_category_cloud),
        Map.entry("computer",      R.drawable.ic_category_computer),
        Map.entry("trash",         R.drawable.ic_category_delete),
        Map.entry("bus",           R.drawable.ic_category_bus),
        Map.entry("auto",          R.drawable.ic_category_car),
        Map.entry("navigation",    R.drawable.ic_category_explore),
        Map.entry("plugins",       R.drawable.ic_category_extension),
        Map.entry("heart",         R.drawable.ic_category_heart),
        Map.entry("flight",        R.drawable.ic_category_flight),
        Map.entry("traffic",       R.drawable.ic_category_traffic),
        Map.entry("drink",         R.drawable.ic_category_bar),
        Map.entry("food",          R.drawable.ic_category_food),
        Map.entry("coffee",        R.drawable.ic_category_cafe),
        Map.entry("filesystem",    R.drawable.ic_category_folder),
        Map.entry("games",         R.drawable.ic_category_games),
        Map.entry("social",        R.drawable.ic_category_group),
        Map.entry("home",          R.drawable.ic_category_home),
        Map.entry("info",          R.drawable.ic_category_info),
        Map.entry("internet",      R.drawable.ic_category_language),
        Map.entry("locked",        R.drawable.ic_category_lock),
        Map.entry("notes",         R.drawable.ic_category_edit),
        Map.entry("video",         R.drawable.ic_category_movie),
        Map.entry("audio",         R.drawable.ic_category_music),
        Map.entry("images",        R.drawable.ic_category_photo),
        Map.entry("media",         R.drawable.ic_category_play),
        Map.entry("education",     R.drawable.ic_category_school),
        Map.entry("search",        R.drawable.ic_category_search),
        Map.entry("security",      R.drawable.ic_category_security),
        Map.entry("configuration", R.drawable.ic_category_settings),
        Map.entry("shopping",      R.drawable.ic_category_shopping),
        Map.entry("smartphone",    R.drawable.ic_category_smartphone),
        Map.entry("starred",       R.drawable.ic_category_star),
        Map.entry("calendar",      R.drawable.ic_category_calendar),
        Map.entry("work",          R.drawable.ic_category_work)
    );

    public static String oldToNewCategoryName(final String oldCategoryName) {
        switch (oldCategoryName) {
            case "MISC":
            case "UNMOVED":     return "misc";
            case "SHORTCUTS":   return "shortcuts";
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
