package hc.utils.cache;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryLruCache<K, T>
{

    private LinkedNode<K, T> first = null;

    private LinkedNode<K, T> tail = null;

    private final HashMap<K, LinkedNode<K, T>> cache = new HashMap<>();

    private final int maxCacheSize;

    private final ReentrantLock lock = new ReentrantLock();

    public MemoryLruCache(int maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
    }

    public void releaseCache(K k){
        lock.lock();
        try
        {
            LinkedNode<K, T> remove = cache.remove(k);
            if (remove != null){
                LinkedNode<K, T> last = remove.getLast();
                LinkedNode<K, T> next = remove.getNext();
                if (last != null){
                    last.setNext(next);
                }
                if (next != null){
                    next.setLast(last);
                }
                remove.setLast(null);
                remove.setNext(null);
            }
        }finally
        {
            lock.unlock();
        }
    }


    public void addCache(K k, T t)
    {
        lock.lock();
        try
        {
            if (cache.containsKey(k)){
                return ;
            }
            LinkedNode<K, T> node = new LinkedNode<>(t, k);
            if (first == null && tail == null)
            {
                first = node;
                tail = node;
                cache.put(k, node);
                return;
            }
            if (cache.size() >= maxCacheSize)
            {
                LinkedNode<K, T> oldTail = tail;
                tail = oldTail.getLast();
                tail.setNext(null);
                cache.remove(oldTail.getKey());
            }
            assert first != null;
            insertFirst(node);
            cache.put(k, node);
        }finally
        {
            lock.unlock();
        }
    }



    public void print(){
        printDfs(first);
        System.out.println("tail: " + tail);
    }

    private void printDfs(LinkedNode<K, T> node){
        if (node == null){
            return ;
        }
        System.out.println(node);
        printDfs(node.getNext());
    }

    public T findCache(K k)
    {
        lock.lock();
        try
        {
            LinkedNode<K, T> node = cache.get(k);
            if (node == null){
                return null;
            }
            if (first.getKey().equals(node.getKey())){
                return node.getCache();
            }
            LinkedNode<K, T> last = node.getLast();
            LinkedNode<K, T> next = node.getNext();
            if (last != null)
            {
                last.setNext(next);
            }
            if (next != null)
            {
                next.setLast(last);
            }else {
                tail = last;
            }
            node.setLast(null);
            insertFirst(node);
            return node.getCache();
        }finally
        {
            lock.unlock();
        }
    }

    private void insertFirst(LinkedNode<K, T> node){
        first.setLast(node);
        node.setNext(first);
        first = node;
    }
}
