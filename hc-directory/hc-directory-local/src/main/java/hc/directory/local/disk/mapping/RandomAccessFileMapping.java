package hc.directory.local.disk.mapping;

import lombok.Data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class RandomAccessFileMapping implements DiskMapping
{

    private final RandomAccessFile channel;

    private final ReentrantLock lock;

    private final long offset;

    private int position = 0;

    private final int size;

    public RandomAccessFileMapping(RandomAccessFile channel, ReentrantLock lock, long offset, int size)
    {
        this.channel = channel;
        this.lock = lock;
        this.offset = offset;
        this.size = size;
        lock.lock();
        try
        {
            if (channel.length() < offset + size)
            {
                channel.seek(channel.length());
                channel.write(new byte[(int) (offset + size - channel.length())]);
            }
        }catch (IOException e){
            throw new hc.utils.errors.IOException(e);
        }finally
        {
            lock.unlock();
        }
    }

    @Override
    public void force()
    {
        // nothing to do
    }

    @Override
    public void put(byte[] bytes)
    {
        lock.lock();
        try
        {
            channel.seek(offset + position);
            channel.write(bytes);
        }
        catch (IOException e)
        {
            throw new hc.utils.errors.IOException(e);
        }finally
        {
            lock.unlock();
        }
    }

    @Override
    public void position(int newPosition)
    {
        this.position = newPosition;
    }

    @Override
    public void get(byte[] bytes)
    {
        lock.lock();
        try
        {
            channel.seek(offset);
            channel.read(bytes);
        }
        catch (IOException e)
        {
            throw new hc.utils.errors.IOException(e);
        }finally
        {
            lock.unlock();
        }
    }
}
