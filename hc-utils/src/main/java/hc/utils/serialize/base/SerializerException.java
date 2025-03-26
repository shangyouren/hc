package hc.utils.serialize.base;

public class SerializerException extends RuntimeException
{

    public SerializerException(byte code, byte real){
        super("Can not serializer code " + code + " from real " + real);
    }

    public SerializerException(String mes){
        super(mes);
    }
}
