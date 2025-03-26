package hc.utils.serialize.base;

import java.util.concurrent.atomic.AtomicInteger;

public class ByteSerializer extends BaseObjectSerializer<Byte>
{
    @Override
    protected Class<Byte> clazz()
    {
        return Byte.class;
    }

    @Override
    protected byte code()
    {
        return CODE_BYTE;
    }

    @Override
    protected void serializer(Byte aByte, byte[] bytes, int offset)
    {
        bytes[offset] = aByte;
    }

    @Override
    protected byte[] len(Byte aByte, AtomicInteger len)
    {
        len.set(1);
        return null;
    }

    @Override
    protected Byte instance(byte[] bytes, int offset, int len)
    {
        return bytes[offset];
    }
}
