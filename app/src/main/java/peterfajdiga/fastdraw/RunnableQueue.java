package peterfajdiga.fastdraw;

import java.util.LinkedList;
import java.util.Queue;

public class RunnableQueue implements Postable {
    private final Queue<Runnable> queue = new LinkedList<>();

    @Override
    public void post(final Runnable f) {
        queue.add(f);
    }

    public void runAll() {
        while (!queue.isEmpty()) {
            final Runnable f = queue.poll();
            assert f != null; // because queue was not empty
            f.run();
        }
    }
}
