package hc.rpc.service;

import io.netty.buffer.ByteBuf;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RpcObjectMapped<T>
{
    public abstract void write(T t, ByteBuf buf);

    public abstract T read(ByteBuf buf, int len);

    public abstract int len(T t);

    public RpcObjectMapped(Class<T> clazz){
        this.clazz = clazz;
        register(this);
    }

    private final Class<T> clazz;


    public static final ConcurrentHashMap<String, RpcObjectMapped<?>> REGISTER = new ConcurrentHashMap<>();

    public static void register(RpcObjectMapped<?> mapped){
        REGISTER.putIfAbsent(mapped.clazz.getCanonicalName(), mapped);
    }

    public static RpcObjectMapped<?> find(String className){
        return REGISTER.get(className);
    }
}
