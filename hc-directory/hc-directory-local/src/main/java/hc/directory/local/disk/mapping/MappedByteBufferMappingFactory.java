package hc.directory.local.disk.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MappedByteBufferMappingFactory implements DiskMappingFactory
{

    private final Map<File, FileChannel> fileFileChannelMap = new ConcurrentHashMap<>();


    @Override
    public DiskMapping create(File file, long blockIndex, int diskBlockSize) throws IOException
    {
        fileFileChannelMap.computeIfAbsent(file, f -> {
            RandomAccessFile open = null;
            try {
                open = new RandomAccessFile(f, "rw");
            } catch (FileNotFoundException e) {
                throw new hc.utils.errors.IOException(e);
            }
            FileChannel channel = open.getChannel();
            return channel;
        });
        FileChannel channel = fileFileChannelMap.get(file);
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, blockIndex, diskBlockSize);
        return new MappedByteBufferMapping(map);
    }
}
