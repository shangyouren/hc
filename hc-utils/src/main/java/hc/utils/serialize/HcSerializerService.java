package hc.utils.serialize;

import hc.utils.serialize.base.ObjectSerializer;

public class HcSerializerService implements SerializerService
{

    private final ObjectSerializer objectSerializer = new ObjectSerializer();

    @Override
    public byte[] toBytes(Object o)
    {
        return objectSerializer.realSerializer(o);
    }

    @Override
    public Object instance(byte[] bytes)
    {
        return objectSerializer.realInstance(bytes, 0);
    }
}
