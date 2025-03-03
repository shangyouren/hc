package hc.utils.convert;

import java.nio.ByteBuffer;

public class DataTransformUtil
{

    public static final int INT_SIZE = Integer.SIZE / Byte.SIZE;

    public static final int LONG_SIZE = Long.SIZE / Byte.SIZE;

    public static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

    public static final int DOUBLE_SIZE = Double.SIZE / Byte.SIZE;

    public static final int SHORT_SIZE = Short.SIZE / Byte.SIZE;

    public static final int BYTE_SIZE = 1;

    public static void intToBytes(int a, byte[] bytes, int offset){
        ByteBuffer.wrap(bytes, offset, INT_SIZE).putInt(a);
    }


    public static int bytesToInt(byte[] a, int seek, int length){
        return ByteBuffer.wrap(a, seek, INT_SIZE).getInt();
    }

    public static void longToByteArray(long value, byte[] bytes, int offset) {
        ByteBuffer.wrap(bytes, offset, LONG_SIZE).putLong(value);
    }

    public static long byteArrayToLong(byte[] bytes, int seek, int length) {
        return ByteBuffer.wrap(bytes, seek, LONG_SIZE).getLong();

    }

    public static void floatToByteArray(float value, byte[] bytes, int offset) {
        ByteBuffer.wrap(bytes, offset, FLOAT_SIZE).putFloat(value);
    }

    public static float byteArrayToFloat(byte[] bytes, int seek, int length) {
        return ByteBuffer.wrap(bytes, seek, FLOAT_SIZE).getFloat();
    }

    public static void doubleToByteArray(double value, byte[] bytes, int offset) {
        ByteBuffer.wrap(bytes, offset, DOUBLE_SIZE).putDouble(value);
    }

    public static double byteArrayToDouble(byte[] bytes, int seek, int length) {
        return ByteBuffer.wrap(bytes, seek, DOUBLE_SIZE).getDouble();
    }

    public static void shortToByteArray(short value, byte[] bytes, int offset) {
        ByteBuffer.wrap(bytes, offset, SHORT_SIZE).putShort(value);
    }

    public static short byteArrayToShort(byte[] bytes, int seek, int length) {
        return ByteBuffer.wrap(bytes, seek, SHORT_SIZE).getShort();
    }

}
