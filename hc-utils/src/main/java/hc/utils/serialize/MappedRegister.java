package hc.utils.serialize;

import java.util.concurrent.ConcurrentHashMap;

public class MappedRegister
{

    private static final ConcurrentHashMap<String, RpcObjectMapped<?>> REGISTER = new ConcurrentHashMap<>();

    public static void register(RpcObjectMapped<?> mapped){
        REGISTER.putIfAbsent(mapped.cla().getCanonicalName(), mapped);
    }

    public static RpcObjectMapped<?> find(String className){
        return REGISTER.get(className);
    }

}
