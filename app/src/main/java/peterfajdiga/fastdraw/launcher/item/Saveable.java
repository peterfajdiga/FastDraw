package peterfajdiga.fastdraw.launcher.item;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public interface Saveable {
    void toFile(File file) throws java.io.IOException;
    String getFilename();

    static void writeString(@NonNull final FileOutputStream fos, @NonNull final String string) throws java.io.IOException {
        final byte[] stringBytes = string.getBytes();
        fos.write(ByteBuffer.allocate(4).putInt(stringBytes.length).array());
        fos.write(stringBytes);
    }

    static String readString(@NonNull final InputStream fis) throws java.io.IOException {
        final byte[] stringLengthBytes = new byte[4];
        fis.read(stringLengthBytes);
        int stringLength = ByteBuffer.wrap(stringLengthBytes).getInt();

        final byte[] stringBytes = new byte[stringLength];
        fis.read(stringBytes);
        return new String(stringBytes);
    }

    class LeftoverException extends Exception {}
}
