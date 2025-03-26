package hc.utils.serialize.base;


import hc.utils.convert.DataTransformUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class LongSerializer extends BaseObjectSerializer<Long>
{
    @Override
    protected Class<Long> clazz()
    {
        return Long.class;
    }

    @Override
    protected byte code()
    {
        return CODE_LONG;
    }

    @Override
    protected void serializer(Long aLong, byte[] bytes, int offset)
    {
        DataTransformUtil.longToByteArray(aLong, bytes, offset);
    }

    @Override
    protected byte[] len(Long aLong, AtomicInteger len)
    {
        len.set(8);
        return null;
    }

    @Override
    protected Long instance(byte[] bytes, int offset, int len)
    {
        return DataTransformUtil.byteArrayToLong(bytes, offset, len);
    }
}
