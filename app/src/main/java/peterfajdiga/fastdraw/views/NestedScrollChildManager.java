package peterfajdiga.fastdraw.views;

import android.graphics.Rect;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class NestedScrollChildManager {
    private RecyclerView contentContainer;
    private ContentHeightGetter contentHeightGetter;

    private final Rect targetVisibleRectTmp = new Rect();

    public void setup(
        @Nullable final RecyclerView contentContainer,
        @Nullable final ContentHeightGetter contentHeightGetter
    ) {
        this.contentHeightGetter = contentHeightGetter;
        this.contentContainer = contentContainer;
    }

    public int getUnscrolledHeight() {
        return Math.max(0, getContentHeight() - getVisibleHeight());
    }

    public int getVisibleHeight() {
        if (contentContainer == null) {
            return 0;
        }
        contentContainer.getGlobalVisibleRect(targetVisibleRectTmp);
        return targetVisibleRectTmp.height();
    }

    public int getContentHeight() {
        if (contentHeightGetter == null) {
            return 0;
        }
        return contentHeightGetter.getContentHeight();
    }

    public int getVerticalScrollOffset() {
        if (contentContainer == null) {
            return 0;
        }
        return contentContainer.computeVerticalScrollOffset();
    }

    public interface ContentHeightGetter {
        int getContentHeight();
    }
}
