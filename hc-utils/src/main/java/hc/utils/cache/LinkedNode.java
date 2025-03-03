package hc.utils.cache;

import lombok.Data;

@Data
public class LinkedNode<K, T> {

    private LinkedNode<K, T> last;

    private LinkedNode<K, T> next;

    private final T cache;

    private final K key;

    private int used = 1;

    private long lastTime = System.currentTimeMillis();

    public String toString(){
        return String.format(
                "%s -> (%s:%s) -> %s",
                last == null ? "null" : last.getKey(),
                key, cache,
                next == null ? "null" : next.getKey());
    }
}
