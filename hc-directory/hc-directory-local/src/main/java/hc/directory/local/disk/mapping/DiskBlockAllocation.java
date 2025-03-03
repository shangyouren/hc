package hc.directory.local.disk.mapping;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import hc.directory.local.disk.collect.queue.DiskLongQueue;
import hc.utils.cache.MemoryLruCache;
import hc.utils.convert.ProjectUtils;
import hc.utils.errors.ArrayDisplacementException;
import hc.utils.errors.CodeException;
import hc.utils.errors.FileLengthException;
import lombok.Data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class DiskBlockAllocation<H extends SerializeHeader, S extends Serialize>
{

    private final File baseDir;

    public static final String SUFFIX = ".DB_LK_T";

    private final int diskBlockSize;

    private final int blockNumberInFile;

    private final Deserialize<H, S> deserialize;

    private final MemoryLruCache<Long, DiskBlock<H, S>> blockCache;

    private final DiskMappingFactory mappingFactory;

    private final DiskLongQueue releaseDisk;

    public DiskBlockAllocation(
            File baseDir,
            int diskBlockSize,
            Deserialize<H, S> deserialize,
            int blockNumberInFile,
            int cacheSize,
            DiskMappingFactory mappingFactory,
            boolean releaseQueueSyncInstantly
    ){
        this.releaseDisk = new DiskLongQueue(releaseQueueSyncInstantly, new File(baseDir, "releaseDisk.qe"));
        this.mappingFactory = mappingFactory;
        if (cacheSize > 0){
            blockCache = new MemoryLruCache<>(cacheSize);
        }else {
            blockCache = null;
        }
        checkMetadata(diskBlockSize, baseDir, blockNumberInFile);
        this.deserialize = deserialize;
        this.blockNumberInFile = blockNumberInFile;
        this.diskBlockSize = diskBlockSize;
        this.baseDir = baseDir;
        File[] files = this.baseDir.listFiles();
        targetFiles = new ArrayList<>();
        len = 0;
        if (files == null){
            File next = next(new File("0" + SUFFIX));
            targetFiles.add(next);
            return;
        }
        for (File file : files)
        {
            File[] childTargetFile = file.listFiles();
            if (childTargetFile == null){
                continue;
            }
            for (File one : childTargetFile)
            {
                if (one.getName().endsWith(SUFFIX)){
                    targetFiles.add(one);
                    long fileLen = one.length();
                    if (fileLen % diskBlockSize != 0){
                        throw new FileLengthException(one);
                    }
                    len += fileLen;
                }
            }
        }
        if (targetFiles.isEmpty()){
            File next = next(new File("0" + SUFFIX));
            targetFiles.add(next);
            return;
        }
        targetFiles.sort(Comparator.comparing(File::getName));

    }

    private void checkMetadata(int diskBlockSize, File baseDir, int blockNumberInFile){
        try
        {
            File metadataFile = new File(baseDir, "metadata.json");
            if (!metadataFile.isFile())
            {
                try (FileOutputStream os = new FileOutputStream(metadataFile))
                {
                    Map<String, Object> metadataMap = new HashMap<>(2);
                    metadataMap.put("diskBlockSize", diskBlockSize);
                    metadataMap.put("blockNumberInFile", blockNumberInFile);
                    os.write(JSON.toJSONString(metadataMap).getBytes(StandardCharsets.UTF_8));
                }
                return ;
            }
            try (FileInputStream is = new FileInputStream(metadataFile))
            {
                String metadata = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                JSONObject metadataObj = JSON.parseObject(metadata);
                int diskBlockSize1 = Integer.parseInt(metadataObj.get("diskBlockSize").toString());
                if (diskBlockSize1 != diskBlockSize)
                {
                    throw new CodeException();
                }
                int blockNumberInFile1 = Integer.parseInt(metadataObj.get("blockNumberInFile").toString());
                if (blockNumberInFile1 != blockNumberInFile){
                    throw new CodeException();
                }
            }
        }catch (IOException e){
            throw new hc.utils.errors.IOException(e);
        }
    }

    private List<File> targetFiles;

    private long len;

    private final ReentrantLock lock = new ReentrantLock();

    public DiskBlock<H, S> newDiskBlock(){
        lock.lock();
        try {
            File file = targetFiles.get(targetFiles.size() - 1);
            if (file.length() / diskBlockSize >= blockNumberInFile) {
                File next = next(file);
                targetFiles.add(next);
                file = next;
            }
            ProjectUtils.checkFile(file);
            long emptyDisk = this.releaseDisk.pop();
            long blockId = emptyDisk == -1 ? len : emptyDisk;
            try {
                DiskBlock<H, S> block = getBlockById(blockId, file, file.length());
                len += diskBlockSize;
                return block;
            } catch (IOException exception) {
                throw new hc.utils.errors.IOException(exception);
            }
        }finally {
            lock.unlock();
        }
    }

    public DiskBlock<H, S> read(long id){
        if (id > len){
            throw new ArrayDisplacementException(DiskBlockAllocation.class.getCanonicalName(), "ID SEEK", id +"");
        }
        long fileIndex = id / ((long) blockNumberInFile * diskBlockSize) + 1;
        File file = getFileWithIndex((int) fileIndex);
        long blockIndex = id % ((long) blockNumberInFile * diskBlockSize);
        try
        {
            return getBlockById(id, file, blockIndex);
        }catch (IOException exception){
            throw new hc.utils.errors.IOException(exception);
        }
    }

    private File next(File last){
        int fileIndex = Integer.parseInt(last.getName().replace(SUFFIX, ""));
        int nextIndex = fileIndex + 1;
        return getFileWithIndex(nextIndex);
    }

    private File getFileWithIndex(int fileIndex){
        return new File(baseDir, fileIndex / 100 + File.separator + fileIndex + SUFFIX);
    }

    @SuppressWarnings("all")
    private DiskBlock<H, S> getBlockById(long blockId, File file, long blockIndex) throws IOException
    {
        if (blockCache == null){
            DiskMapping map = mappingFactory.create(file, blockIndex, diskBlockSize);
            DiskBlock<H, S> block = new DiskBlock<>(map, blockId, diskBlockSize, deserialize);
            return block;
        }
        DiskBlock<H, S> cache = blockCache.findCache(blockId);
        if (cache != null){
            return cache;
        }
        DiskMapping map = mappingFactory.create(file, blockIndex, diskBlockSize);
        DiskBlock<H, S> block = new DiskBlock<>(map, blockId, diskBlockSize, deserialize);
        blockCache.addCache(blockId, block);
        return block;
    }

    public void release(Long id){
        DiskBlock<H, S> block = read(id);
        block.clear();
        this.releaseDisk.put(id);
    }


}
