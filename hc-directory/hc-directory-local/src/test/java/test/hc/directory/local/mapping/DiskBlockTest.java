package test.hc.directory.local.mapping;

import hc.directory.local.disk.collect.btree.TreeLeaf;
import hc.directory.local.disk.collect.btree.TreeLineDeserialize;
import hc.directory.local.disk.mapping.DiskBlock;
import hc.directory.local.disk.mapping.MappedByteBufferMapping;
import hc.directory.local.disk.mapping.Serialize;
import hc.directory.local.disk.mapping.SerializeHeader;
import hc.directory.local.disk.pojo.FileBlockLeafDeserialize;
import org.junit.Assert;
import org.junit.Test;
import test.hc.directory.local.btree.SerializeTest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DiskBlockTest {


    @Test
    public void test() throws IOException {
        File tempFile = File.createTempFile("test-block", ".dblk");
        MappedByteBuffer map;
        try (
                RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
                FileChannel fc = raf.getChannel();

        ){
            map = fc.map(FileChannel.MapMode.READ_WRITE, 0, 16 * 1024);
        }
        long time = System.currentTimeMillis();
        DiskBlock<SerializeHeader, Serialize> block = new DiskBlock<>(new MappedByteBufferMapping(map), 0, 16 * 1024, new TreeLineDeserialize(new FileBlockLeafDeserialize()));
        List<String> expect = new ArrayList<>();
        int finalI = 0;
        for (int i = 0; i < 1000; i++){
            boolean add = block.add(SerializeTest.createLeaf("test-" + i));
            if (add){
                expect.add("test-" + i);
                finalI = i;
            }
        }
        AtomicInteger i = new AtomicInteger();
        block.foreach((data, offset) -> {
            Assert.assertEquals(((TreeLeaf) data).getName(), "test-" + i);
            i.getAndIncrement();
            return null;
        });
        Assert.assertEquals(i.get(), finalI + 1);
        check(block, expect);
        block.delete(block.metadataLen());
        expect.remove("test-0");
        check(block, expect);
        block.add(SerializeTest.createLeaf("hello"));
        expect.add("hello");
        check(block, expect);

        block.delete(block.metadataLen());
        expect.remove("test-1");
        check(block, expect);
        block.edit(SerializeTest.createLeaf("test-2"), block.metadataLen());
        check(block, expect);

        block.delete(block.metadataLen());
        expect.remove("test-3");
        check(block, expect);
        block.add(SerializeTest.createLeaf("hello-2"));
        expect.add("hello-2");
        check(block, expect);
        System.out.println("存储块映射测试通过，耗时：" + (System.currentTimeMillis() - time));
    }

    private void check(DiskBlock<SerializeHeader, Serialize> block, List<String> expect){
        ArrayList<String> temp = new ArrayList<>(expect);
        block.foreach((data, offset) -> {
            String name = ((TreeLeaf) data).getName();
            if (!temp.contains(name)){
                throw new RuntimeException();
            }
            temp.remove(name);
            return null;
        });
        if (!temp.isEmpty()){
            throw new RuntimeException();
        }
    }
}
