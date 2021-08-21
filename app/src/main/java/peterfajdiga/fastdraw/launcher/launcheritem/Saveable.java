package peterfajdiga.fastdraw.launcher.launcheritem;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface Saveable {
    void save(OutputStream out) throws java.io.IOException;

    static void writeString(@NonNull final OutputStream out, @NonNull final String string) throws java.io.IOException {
        final byte[] stringBytes = string.getBytes();
        out.write(ByteBuffer.allocate(4).putInt(stringBytes.length).array());
        out.write(stringBytes);
    }

    static String readString(@NonNull final InputStream in) throws java.io.IOException {
        final byte[] stringLengthBytes = new byte[4]; // 4 bytes to hold an int
        in.read(stringLengthBytes);
        int stringLength = ByteBuffer.wrap(stringLengthBytes).getInt();

        final byte[] stringBytes = new byte[stringLength];
        in.read(stringBytes);
        return new String(stringBytes);
    }

    class LeftoverException extends Exception {}
}
