package peterfajdiga.fastdraw;

import android.content.Context;
import android.graphics.drawable.Drawable;

public final class Common {

    private Common() {}

    public static Drawable getCategoryIcon(Context context, String categoryName) {
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
            case "CONTACTS":
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
