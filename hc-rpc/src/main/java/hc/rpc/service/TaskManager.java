package hc.rpc.service;

import hc.rpc.pojo.RpcPackage;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.*;

public class TaskManager
{

    private static final Map<Long, Task> TASK_MAP = new ConcurrentHashMap<>();

    public static void addTask(long id, CompletableFuture<RpcPackage> listener, long timeout)
    {
        TASK_MAP.put(id, new Task(listener, System.currentTimeMillis() + timeout));
    }


    public static CompletableFuture<RpcPackage> getAndRemoveResponseListener(long id)
    {
        Task task = TASK_MAP.remove(id);
        if (task != null){
            return task.getFuture();
        }
        return null;
    }

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1, (ThreadFactory) Thread::new);

    static {
        SCHEDULED_EXECUTOR_SERVICE.schedule(TaskManager::scan, 60, TimeUnit.SECONDS);
    }

    private static void scan(){
        try
        {
            TASK_MAP.forEach((key, task) ->
            {
                if (task.expireTime < System.currentTimeMillis()){
                    Task remove = TASK_MAP.remove(key);
                    remove.getFuture().complete(RpcPackage.exceptionResponse(key, new TimeoutException()));
                }
            });
        }finally
        {
            SCHEDULED_EXECUTOR_SERVICE.schedule(TaskManager::scan, 60, TimeUnit.SECONDS);
        }
    }

    @Data
    private static class Task{

        private final CompletableFuture<RpcPackage> future;

        private final long expireTime;

    }


}
