package hc.utils.serialize;

public interface SerializerService
{

    byte[] toBytes(Object o);

    Object instance(byte[] bytes);

}
