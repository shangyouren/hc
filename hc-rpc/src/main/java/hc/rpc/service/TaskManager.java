package hc.rpc.service;

import hc.rpc.pojo.RpcPackage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager
{

    private static final Map<Long, CompletableFuture<RpcPackage>> TASK_MAP = new ConcurrentHashMap<>();

    public static void addTask(long id, CompletableFuture<RpcPackage> listener, long timeout)
    {
        TASK_MAP.put(id, listener);
    }


    public static CompletableFuture<RpcPackage> getAndRemoveResponseListener(long id)
    {
        return TASK_MAP.get(id);
    }
}
