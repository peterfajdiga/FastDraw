package peterfajdiga.fastdraw;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Predicate;

import java.util.Set;

public class PrefMap {

    private SharedPreferences prefs;

    public PrefMap(final Context context, final String name) {
        prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public String getString(final String key, @Nullable final String defValue) {
        return prefs.getString(key, defValue);
    }

    public void putString(final String key, @Nullable final String value) {
        final SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public int getInt(final String key, final int defValue) {
        return prefs.getInt(key, defValue);
    }

    public int getIntCreate(final String key, final int defValue) {
        final int storedValue = prefs.getInt(key, defValue);
        if (storedValue == defValue) {
            putInt(key, defValue);
        }
        return storedValue;
    }

    public void putInt(final String key, final int value) {
        final SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.apply();
    }

    public Set<String> getKeys() {
        return prefs.getAll().keySet();
    }

    public void remove(@NonNull final String key) {
        final SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.remove(key);
        prefsEditor.apply();
    }

    /**
     * remove unused entries
     *
     * @param filter should return true for items to remove
     */
    public void clean(final Predicate<String> filter) {
        final SharedPreferences.Editor prefsEditor = prefs.edit();

        for (String key : getKeys()) {
            if (filter.test(key)) {
                prefsEditor.remove(key);
            }
        }
        prefsEditor.apply();
    }
}
