package peterfajdiga.fastdraw.dragdrop;

import java.util.LinkedList;
import java.util.Queue;

import peterfajdiga.fastdraw.launcher.CategoryAdapter;

public class DragEndQueue implements CategoryAdapter.DragEndService {
    private final Queue<Runnable> queue = new LinkedList<>();

    @Override
    public void post(final Runnable f) {
        queue.add(f);
    }

    public void onDragEnd() {
        while (!queue.isEmpty()) {
            final Runnable f = queue.poll();
            assert f != null; // because queue was not empty
            f.run();
        }
    }
}
