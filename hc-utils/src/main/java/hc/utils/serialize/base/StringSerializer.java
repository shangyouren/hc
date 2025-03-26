package hc.utils.serialize.base;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

class StringSerializer extends BaseObjectSerializer<String>
{
    @Override
    public byte code()
    {
        return CODE_STRING;
    }

    @Override
    protected void serializer(String s, byte[] bytes, int offset)
    {
        byte[] bytes1 = s.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytes1, 0, bytes, offset, bytes1.length);
    }

    @Override
    protected byte[] len(String s, AtomicInteger len)
    {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected Class<String> clazz()
    {
        return String.class;
    }


    @Override
    public String instance(byte[] bytes, int offset, int len)
    {
        byte[] fr = new byte[len];
        System.arraycopy(bytes, offset, fr, 0, len);
        return new String(fr, StandardCharsets.UTF_8);
    }
}
