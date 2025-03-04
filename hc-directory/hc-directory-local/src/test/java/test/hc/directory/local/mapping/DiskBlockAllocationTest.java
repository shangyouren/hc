package test.hc.directory.local.mapping;

import hc.directory.local.collect.btree.TreeLineDeserialize;
import hc.directory.local.disk.mapping.*;
import hc.directory.local.mapping.*;
import hc.directory.local.pojo.FileBlockLeafDeserialize;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiskBlockAllocationTest {
    @Test
    public void test() throws IOException {
        File tempFile = createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempFile, 4 * 1024, new TreeLineDeserialize(new FileBlockLeafDeserialize()), 5, 0,
                new RandomAccessFileMappingFactory(), false
        );
        long time = System.currentTimeMillis();
        List<Long> blockIds = new ArrayList<>();
        for (int i = 0; i < 100; i++){
            DiskBlock<SerializeHeader, Serialize> block = allocation.newDiskBlock();
            blockIds.add(block.id());
            if (i % 10000 == 0) {
                System.out.println("find " + i + " " + (System.currentTimeMillis() - time));
            }
        }
        for (int i = 0; i < 100; i++){
            DiskBlock<SerializeHeader, Serialize> read = allocation.read(blockIds.get(i));
            Assert.assertEquals(read.id(), ((long) blockIds.get(i)));
            if (i % 10000 == 0) {
                System.out.println("find " + i + " " + (System.currentTimeMillis() - time));
            }
        }
        Assert.assertEquals(allocation.getTargetFiles().size(), 2);
    }

    @Test
    public void test2() throws IOException {
        File tempFile = createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempFile, 16 * 1024, new TreeLineDeserialize(new FileBlockLeafDeserialize()), 5, 100,
                new MappedByteBufferMappingFactory(), false
        );
        List<Long> blockIds = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            DiskBlock<SerializeHeader, Serialize> block = allocation.newDiskBlock();
            blockIds.add(block.id());
        }
        for (int i = 0; i < 10; i++){
            DiskBlock<SerializeHeader, Serialize> read = allocation.read(blockIds.get(i));
            Assert.assertEquals(read.id(), ((long) blockIds.get(i)));
        }
        Assert.assertEquals(allocation.getTargetFiles().size(), 2);
    }

    public static File createTempDirectory() throws IOException {
        File tempFile = File.createTempFile("test_directory_test", ".dir").getParentFile();
        tempFile = new File(tempFile, "test_directory_test_" + UUID.randomUUID());
        if (!tempFile.isDirectory()){
            boolean mkdirs = tempFile.mkdirs();
            if (!mkdirs){
                throw new IOException();
            }
        }
        return tempFile;
    }
}
