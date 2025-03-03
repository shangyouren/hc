//
//
//
//package hc.utils.cache;
//
//import cn.hutool.cache.CacheUtil;
//import cn.hutool.cache.impl.LFUCache;
//
//import java.util.HashMap;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class MemoryLurCache<K, T>
//{
//
//    private final LFUCache<K, T> cache;
//
//    public MemoryLurCache(int maxCacheSize)
//    {
//        cache = CacheUtil.newLFUCache(maxCacheSize);
//    }
//
//
//    public void addCache(K k, T t)
//    {
//        cache.put(k, t);
//    }
//
//
//
//
//    public T findCache(K k)
//    {
//        return cache.get(k);
//    }
//
//}
