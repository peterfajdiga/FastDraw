package peterfajdiga.fastdraw.prefs;

import android.content.Context;

import androidx.core.util.Predicate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrefMapCached extends PrefMap {

    public PrefMapCached(final Context context, final String name) {
        super(context, name);
    }

    private Map<String, Integer> cache_int = new ConcurrentHashMap<>();

    @Override
    public int getInt(final String key, final int defValue) {
        final Integer cachedValue = cache_int.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }

        // if not yet cached, retrieve it from prefs and cache it
        final int storedValue = super.getInt(key, defValue);
        cache_int.put(key, storedValue);
        return storedValue;
    }

    @Override
    public int getIntCreate(final String key, final int defValue) {
        final Integer cachedValue = cache_int.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }

        // if not yet cached, retrieve it from prefs and cache it
        final int storedValue = super.getIntCreate(key, defValue);
        cache_int.put(key, storedValue);
        return storedValue;
    }

    @Override
    public void putInt(final String key, final int value) {
        cache_int.put(key, value);
        super.putInt(key, value);
    }

    @Override
    public void clean(final Predicate<String> filter) {
        cache_int.clear();
        super.clean(filter);
    }
}
