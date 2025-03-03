package hc.directory.local.disk.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@AllArgsConstructor
public class MappedByteBufferMapping implements DiskMapping
{
    private final MappedByteBuffer buffer;

    @Override
    public void force()
    {
        this.buffer.force();
    }

    @Override
    public void put(byte[] bytes)
    {
        this.buffer.put(bytes);
    }

    @Override
    public void position(int newPosition)
    {
        this.buffer.position(newPosition);
    }

    @Override
    public void get(byte[] bytes)
    {
        this.buffer.get(bytes);
    }
}
