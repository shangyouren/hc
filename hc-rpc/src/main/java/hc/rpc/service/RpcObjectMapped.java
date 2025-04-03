package hc.rpc.service;

import hc.rpc.service.impl.ListRpcObjectMapped;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RpcObjectMapped<T>
{
    public abstract void write(T t, ByteBuf buf);

    public abstract T read(ByteBuf buf, int len);

    public abstract int len(T t);

    public RpcObjectMapped(Class<T> clazz){
        this.clazz = clazz;
    }

    private final Class<T> clazz;


    public static final ConcurrentHashMap<String, RpcObjectMapped<?>> REGISTER = new ConcurrentHashMap<>();

    public static void register(RpcObjectMapped<?> mapped){
        REGISTER.putIfAbsent(mapped.clazz.getCanonicalName(), mapped);
    }

    public static RpcObjectMapped<?> find(String className){
        if (className.equals(List.class.getCanonicalName())){
            return new ListRpcObjectMapped();
        }
        return REGISTER.get(className);
    }
}
