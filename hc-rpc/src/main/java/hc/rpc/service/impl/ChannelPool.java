package hc.rpc.service.impl;

import hc.rpc.pojo.Target;
import io.netty.channel.Channel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelPool
{

    private final Target target;

    public final List<Channel> channels = new LinkedList<>();

    public final ReentrantLock lock = new ReentrantLock();

    private final AtomicInteger index = new AtomicInteger(0);

    public ChannelPool(Target target, Channel channel){
        this.target = target;
        this.channels.add(channel);
    }

    public Target getTarget(){
        return target;
    }

    public void add(Channel channel){
        lock.lock();
        try
        {
            this.channels.add(channel);
        }finally
        {
            lock.unlock();
        }
    }

    public void remove(Channel channel){
        lock.lock();
        try
        {
            int i;
            for (i = 0; i < channels.size(); i++){
                if (channels.get(i).id().asLongText().equals(channel.id().asLongText())){
                    break;
                }
            }
            if (i < channels.size()){
                channels.remove(i);
            }
        }finally
        {
            lock.unlock();
        }
    }

    public Channel findOne(){
        int index = this.index.getAndIncrement();
        if (index < channels.size()){
            return channels.get(index);
        }
        if (channels.isEmpty()){
            return null;
        }
        index = index % channels.size();
        return channels.get(index);
    }

}
