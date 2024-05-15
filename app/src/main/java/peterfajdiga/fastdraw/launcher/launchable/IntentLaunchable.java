package peterfajdiga.fastdraw.launcher.launchable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.LaunchManager;
import peterfajdiga.fastdraw.launcher.StatisticsManager;

public class IntentLaunchable implements Launchable {
    private final Intent intent;
    private final String id;
    private final StatisticsManager statisticsManager;

    public IntentLaunchable(@NonNull final Intent intent, @NonNull final StatisticsManager statisticsManager, @NonNull final String id) {
        this.intent = intent;
        this.id = id;
        this.statisticsManager = statisticsManager;
    }

    @Override
    public void launch(final Context context, final LaunchManager launchManager, final Bundle opts, final Rect clipBounds) {
        final Intent intent = new Intent(this.intent);
        intent.setSourceBounds(clipBounds);
        launchManager.launch(intent, opts);
        statisticsManager.addLaunch(id);
    }
}
