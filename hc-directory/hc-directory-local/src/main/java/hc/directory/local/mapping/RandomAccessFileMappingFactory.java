package hc.directory.local.mapping;

import lombok.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class RandomAccessFileMappingFactory implements DiskMappingFactory
{

    private final Map<File, RandomAccessFile> fileFileChannelMap = new ConcurrentHashMap<>();

    private final Map<File, ReentrantLock> fileLockMap = new ConcurrentHashMap<>();

    private final boolean syncInstantly;

    public RandomAccessFileMappingFactory(){
        this.syncInstantly = false;
    }

    public RandomAccessFileMappingFactory(boolean syncInstantly){
        this.syncInstantly = syncInstantly;
    }


    @Override
    public DiskMapping create(File file, long blockIndex, int diskBlockSize)
    {

        fileFileChannelMap.computeIfAbsent(file, f -> {
            RandomAccessFile open;
            try {
                open = new RandomAccessFile(f, syncInstantly ? "rws" : "rw");
            } catch (FileNotFoundException e) {
                throw new hc.utils.errors.IOException(e);
            }
            fileLockMap.put(f, new ReentrantLock());
            return open;
        });
        RandomAccessFile randomAccessFile = fileFileChannelMap.get(file);
        ReentrantLock lock = fileLockMap.get(file);
        return new RandomAccessFileMapping(randomAccessFile, lock, blockIndex, diskBlockSize);
    }
}
