package hc.directory.local.mapping;

import lombok.AllArgsConstructor;

import java.nio.MappedByteBuffer;

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
