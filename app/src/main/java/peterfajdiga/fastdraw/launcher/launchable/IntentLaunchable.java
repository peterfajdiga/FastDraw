package peterfajdiga.fastdraw.launcher.launchable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;

import peterfajdiga.fastdraw.launcher.LaunchManager;

public class IntentLaunchable implements Launchable {
    private final Intent intent;

    public IntentLaunchable(@NonNull final Intent intent) {
        this.intent = intent;
    }

    @Override
    public void launch(final Context context, final LaunchManager launchManager, final Bundle opts, final Rect clipBounds) {
        final Intent intent = new Intent(this.intent);
        intent.setSourceBounds(clipBounds);
        launchManager.launch(intent, opts);
    }
}
