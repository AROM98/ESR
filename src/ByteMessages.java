import java.io.*;

public class ByteMessages {

    public static byte[] readBytes(InputStream in) throws IOException {
        // Again, probably better to store these objects references in the support class
        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }

    public static void sendBytes(byte[] myByteArray, OutputStream out) throws IOException {
        sendBytes(myByteArray, 0, myByteArray.length, out);
    }

    public static void sendBytes(byte[] myByteArray, int start, int len, OutputStream out) throws IOException {
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        // Other checks if needed.

        // May be better to save the streams in the support class;
        // just like the socket variable.
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }
}
