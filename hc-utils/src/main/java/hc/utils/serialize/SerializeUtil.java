package hc.utils.serialize;

public class SerializeUtil implements SerializerService
{

    public SerializeUtil(String type){
        if ("hc".equals(type)){
            service = new HcSerializerService();
        }else {
            service = new JsonSerializerService();
        }
    }

    private final SerializerService service;

    @Override
    public byte[] toBytes(Object o)
    {
        return service.toBytes(o);
    }

    @Override
    public Object instance(byte[] bytes)
    {
        return service.instance(bytes);
    }


}
