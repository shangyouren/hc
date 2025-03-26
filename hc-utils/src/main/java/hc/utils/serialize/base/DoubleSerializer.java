package hc.utils.serialize.base;


import hc.utils.convert.DataTransformUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class DoubleSerializer extends BaseObjectSerializer<Double>
{
    @Override
    protected Class<Double> clazz()
    {
        return Double.class;
    }

    @Override
    protected byte code()
    {
        return CODE_DOUBLE;
    }

    @Override
    protected void serializer(Double aDouble, byte[] bytes, int offset)
    {
        DataTransformUtil.doubleToByteArray(aDouble, bytes, offset);
    }

    @Override
    protected byte[] len(Double aDouble, AtomicInteger len)
    {
        len.set(8);
        return null;
    }


    @Override
    protected Double instance(byte[] bytes, int offset, int len)
    {
        return DataTransformUtil.byteArrayToDouble(bytes, offset, 8);
    }
}
