package hc.utils.serialize.base;


import hc.utils.convert.DataTransformUtil;

import java.util.concurrent.atomic.AtomicInteger;

abstract class BaseObjectSerializer<T>
{

    public static final byte CODE_STRING = 1;
    public static final byte CODE_BYTE_ARRAY = 2;
    public static final byte CODE_INT = 3;
    public static final byte CODE_DOUBLE = 4;
    public static final byte CODE_LONG = 5;
    public static final byte CODE_FLOAT = 6;
    public static final byte CODE_BIG_DECIMAL = 7;
    public static final byte CODE_DATE = 8;
    public static final byte CODE_LOCAL_DATE_TIME = 9;
    public static final byte CODE_BYTE = 10;
    public static final byte CODE_CHAR = 11;
    public static final byte CODE_SHORT = 12;
    public static final byte CODE_OBJECT = 13;
    public static final byte CODE_MAP = 14;
    public static final byte CODE_LIST = 15;


    protected abstract Class<T> clazz();

    protected abstract byte code();

    protected abstract void serializer(T t, byte[] bytes, int offset);

    protected abstract byte[] len(T t, AtomicInteger len);

    protected abstract T instance(byte[] bytes, int offset, int len);

    protected byte[] realLen(T t, AtomicInteger len){
        byte[] preLenBytes = len(t, len);
        if (preLenBytes == null){
            len.set(len.get() + 5);
            return null;
        }else {
            byte[] fr = new byte[preLenBytes.length + 5];
            fr[0] = code();
            DataTransformUtil.intToBytes(preLenBytes.length, fr, 1);
            System.arraycopy(preLenBytes, 0, fr, 5, preLenBytes.length);
            return fr;
        }
    }

    protected void realSerializer(T t, byte[] bytes, int offset){
        AtomicInteger preLenRef = new AtomicInteger();
        int len;
        byte[] preLen = len(t, preLenRef);
        if (preLen != null){
            len = preLen.length;
        }else {
            len = preLenRef.get();
        }
        bytes[offset] = code();
        DataTransformUtil.intToBytes(len, bytes, offset + 1);
        if (preLen != null)
        {
            System.arraycopy(preLen, 0, bytes, offset + 5, len);
        }else {
            serializer(t, bytes, offset + 5);
        }
    }

    public byte[] realSerializer(T t)
    {
        byte[] fr;
        AtomicInteger preLenRef = new AtomicInteger();
        int len;
        byte[] preLen = len(t, preLenRef);
        if (preLen != null){
            len = preLen.length;
            fr = new byte[preLen.length + 5];
        }else {
            len = preLenRef.get();
            fr = new byte[preLenRef.get() + 5];
        }
        fr[0] = code();
        DataTransformUtil.intToBytes(len, fr, 1);
        if (preLen != null)
        {
            System.arraycopy(preLen, 0, fr, 5, len);
        }else {
            serializer(t, fr, 5);
        }
        return fr;
    }

    public int len(byte[] array, int start){
        byte code = array[start];
        if (code != code())
        {
            throw new SerializerException(code(), code);
        }
        int len = DataTransformUtil.bytesToInt(array, start + 1, 4);
        if (len + start + 5 > array.length)
        {
            throw new SerializerException(code(), code);
        }
        return len + 5;
    }

    public T realInstance(byte[] array, int start)
    {
        byte code = array[start];
        if (code != code())
        {
            throw new SerializerException(code(), code);
        }
        int len = DataTransformUtil.bytesToInt(array, start + 1, 4);
        if (len + start + 5 > array.length)
        {
            throw new SerializerException(code(), code);
        }
        return instance(array, start + 5, len);
    }

}
