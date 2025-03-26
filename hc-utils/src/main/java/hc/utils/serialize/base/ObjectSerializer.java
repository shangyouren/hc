package hc.utils.serialize.base;

import hc.utils.serialize.MappedRegister;
import hc.utils.serialize.RpcObjectMapped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectSerializer extends BaseObjectSerializer<Object>
{

    private static final HashMap<Class<?>, BaseObjectSerializer<?>> CACHE = new HashMap<>();

    private static final HashMap<Byte, BaseObjectSerializer<?>> CACHE2 = new HashMap<>();

    static {
        BaseObjectSerializer<?>[] arrays = new BaseObjectSerializer[]{
                new ByteArraySerializer(),
                new ByteSerializer(),
                new DoubleSerializer(),
                new IntegerSerializer(),
                new LongSerializer(),
                new StringSerializer(),
                new ObjectSerializer()
        };
        for (BaseObjectSerializer<?> array : arrays)
        {
            CACHE.put(array.clazz(), array);
            CACHE2.put(array.code(), array);
        }
    }

    @Override
    protected Class<Object> clazz()
    {
        return Object.class;
    }

    @Override
    protected byte code()
    {
        return CODE_OBJECT;
    }

    @Override
    protected void serializer(Object o, byte[] bytes, int offset)
    {
        throw new SerializerException("Unsupported");
    }

    @SuppressWarnings("all")
    @Override
    protected byte[] len(Object o, AtomicInteger preLenRef)
    {
        RpcObjectMapped mapped = MappedRegister.find(o.getClass().getCanonicalName());
        if (mapped == null){
            throw new SerializerException("Cannot find mapped by class " + o.getClass().getCanonicalName());
        }
        List<Object> objects = mapped.fieldValues(o);
        objects.add(o.getClass().getCanonicalName());
        byte[][] fb = new byte[objects.size()][];
        int i = 0;
        int len = 0;
        for (Object object : objects)
        {
            BaseObjectSerializer serializer = CACHE.get(object.getClass());
            if (serializer == null){
                serializer = CACHE.get(Object.class);
            }
            byte[] bytes = serializer.realSerializer(object);
            fb[i] = bytes;
            i++;
            len += bytes.length;
        }
        byte[] r = new byte[len];
        int ir = 0;
        for (byte[] bytes : fb)
        {
            System.arraycopy(bytes, 0, r, ir, bytes.length);
            ir+= bytes.length;
        }
        return r;
    }

    @SuppressWarnings("all")
    @Override
    protected Object instance(byte[] bytes, int offset, int len)
    {
        List<Object> fields = new ArrayList<>();
        int start = offset;
        while (start < offset + len){
            byte code = bytes[start];
            BaseObjectSerializer serializer = CACHE2.get(code);
            Object o = serializer.realInstance(bytes, start);
            fields.add(o);
            start = start + serializer.len(bytes, start);
        }
        String className = (String) fields.get(fields.size() - 1);
        RpcObjectMapped mapped = MappedRegister.find(className);
        if (mapped == null){
            throw new SerializerException("Cannot find mapped by class " + className);
        }
        Object o = mapped.newInstance();
        mapped.setField(o, fields);
        return o;
    }
}
