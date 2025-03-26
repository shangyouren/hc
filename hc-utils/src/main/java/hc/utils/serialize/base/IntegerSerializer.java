package hc.utils.serialize.base;


import hc.utils.convert.DataTransformUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class IntegerSerializer extends BaseObjectSerializer<Integer>
{
    @Override
    protected Class<Integer> clazz()
    {
        return Integer.class;
    }

    @Override
    protected byte code()
    {
        return CODE_INT;
    }

    @Override
    protected void serializer(Integer integer, byte[] bytes, int offset)
    {
        DataTransformUtil.intToBytes(integer, bytes, offset);
    }

    @Override
    protected byte[] len(Integer integer, AtomicInteger len)
    {
        len.set(4);
        return null;
    }

    @Override
    protected Integer instance(byte[] bytes, int offset, int len)
    {
        return DataTransformUtil.bytesToInt(bytes, offset, 4);
    }
}
