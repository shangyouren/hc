package hc.utils.serialize.base;

import java.util.concurrent.atomic.AtomicInteger;

public class ByteArraySerializer extends BaseObjectSerializer<byte[]>
{
    @Override
    protected Class<byte[]> clazz()
    {
        return byte[].class;
    }

    @Override
    protected byte code()
    {
        return CODE_BYTE_ARRAY;
    }

    @Override
    protected void serializer(byte[] bytes, byte[] bytes2, int offset)
    {
        System.arraycopy(bytes, 0, bytes2, offset, bytes.length);
    }

    @Override
    protected byte[] len(byte[] bytes, AtomicInteger len)
    {
        len.set(bytes.length);
        return null;
    }

    @Override
    protected byte[] instance(byte[] bytes, int offset, int len)
    {
        byte[] fr = new byte[len];
        System.arraycopy(bytes, offset, fr, 0, len);
        return fr;
    }
}
