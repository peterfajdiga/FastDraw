package peterfajdiga.fastdraw.launcher.item;

import java.io.File;

public interface Saveable {
    void toFile(File file) throws java.io.IOException;
    String getTypeKey();
    String getFilename();
}
