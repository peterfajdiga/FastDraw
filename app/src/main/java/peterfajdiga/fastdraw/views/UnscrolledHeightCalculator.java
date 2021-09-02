package peterfajdiga.fastdraw.views;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UnscrolledHeightCalculator {
    private View contentContainer;
    private ContentHeightGetter contentHeightGetter;

    private final Rect targetVisibleRectTmp = new Rect();

    public void setup(
        @Nullable final View contentContainer,
        @Nullable final ContentHeightGetter contentHeightGetter
    ) {
        this.contentHeightGetter = contentHeightGetter;
        this.contentContainer = contentContainer;
    }

    public int getUnscrolledHeight() {
        if (contentHeightGetter == null || contentContainer == null) {
            return 0;
        }
        return Math.max(0, contentHeightGetter.getContentHeight() - getVisibleHeight(contentContainer));
    }

    private int getVisibleHeight(@NonNull final View target) {
        target.getGlobalVisibleRect(targetVisibleRectTmp);
        return targetVisibleRectTmp.height();
    }

    public interface ContentHeightGetter {
        int getContentHeight();
    }
}
