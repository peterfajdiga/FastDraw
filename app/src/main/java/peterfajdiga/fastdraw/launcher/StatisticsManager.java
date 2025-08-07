package peterfajdiga.fastdraw.launcher;

import androidx.annotation.NonNull;

import java.util.HashMap;

import peterfajdiga.fastdraw.launcher.launcheritem.AppItem;

public class StatisticsManager {
    private final HashMap<String, Integer> launchCounts;

    public StatisticsManager(@NonNull final HashMap<String, Integer> launchCounts) {
        this.launchCounts = launchCounts;
    }

    public Integer launchCount(final String id) {
        Integer count = launchCounts.get(id);
        if (count == null) return 0;
        return count;
    }

    public void addLaunch(final String id) {
        Integer count = launchCounts.get(id);
        if (count == null) launchCounts.put(id, 1);
        else launchCounts.put(id, count++);
    }
}
