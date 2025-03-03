package test.hc.directory.local.btree;

import hc.directory.local.disk.collect.btree.*;
import hc.directory.local.disk.constants.EnumFileType;
import hc.directory.local.disk.mapping.*;
import hc.directory.local.disk.pojo.FileBlock;
import hc.directory.local.disk.pojo.FileBlockLeafDeserialize;
import hc.utils.convert.ProjectUtils;
import hc.utils.errors.CodeException;
import org.junit.Assert;
import org.junit.Test;
import test.hc.directory.local.mapping.DiskBlockAllocationTest;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class BPlusTreeTest {


    @Test
    public void testDelete() throws IOException {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                4 * 1024, new TreeLineDeserialize(new FileBlockLeafDeserialize()),
                40000, 10000, new RandomAccessFileMappingFactory(false), false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        long treeId = idList.next();
        BPlusTree tree = new BPlusTree(
                treeId, idList, allocation);
        Random random = new Random();
        try {
            long time = System.currentTimeMillis();
            List<String> keys = new LinkedList<>();
            for (int i = 0; i < 1000; i++) {
                String name = "test-" + random.nextLong();
                tree.add(SerializeTest.createLeaf(name, i));
                keys.add(name);
                if (i % 10000 == 0) {
                    System.out.println("save " + i + " " + (System.currentTimeMillis() - time));
                }
            }
            int i = 0;
            for (String key : keys) {
                i++;
                TreeLeaf find = tree.find(key);
                if (find == null) {
                    System.out.println(treeId);
                    System.out.println(key);
                    throw new RuntimeException();
                }
                if (i % 10000 == 0) {
                    System.out.println("find " + i + " " + (System.currentTimeMillis() - time));
                }
            }
            i = 0;
            for (String key : keys) {
                i++;
                TreeLeaf delete = tree.delete(key);
                Assert.assertEquals(delete.getName(), key);
                if (i % 10000 == 0) {
                    System.out.println("delete " + i + " " + (System.currentTimeMillis() - time));
                }
            }
            System.out.println(System.currentTimeMillis() - time);
        } catch (Throwable e) {
            tree.printTree();
            throw e;
        }
    }

    @Test
    public void testThread() throws IOException {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                4 * 1024, new TreeLineDeserialize(new FileBlockLeafDeserialize()),
                40000, 10000, new MappedByteBufferMappingFactory(), false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        long treeId = idList.next();
        BPlusTree tree = new BPlusTree(
                treeId, idList, allocation);
        Random random = new Random();
        try {
            CountDownLatch latch = new CountDownLatch(10);
            ConcurrentHashMap<String, Boolean> keys = new ConcurrentHashMap<>();
            AtomicInteger count = new AtomicInteger(0);
            long time = System.currentTimeMillis();
            for (int t = 0; t < 10; t++) {
                new Thread(() -> {
                    for (int i = 0; i < 10000; i++) {
                        String name = "test-" + random.nextLong();
                        keys.put(name, true);
                        int finalI = i;
                        tree.addIfAbsent(SerializeTest.createLeaf(name, i), new Consumer<TreeLeaf>() {
                            @Override
                            public void accept(TreeLeaf treeLeaf) {
                                ((FileBlock) treeLeaf).setChildRootId(finalI);
                            }
                        });
                        int i1 = count.incrementAndGet();
                        if (i1 % 10000 == 0) {
                            System.out.println("save " + i1 + " " + (System.currentTimeMillis() - time));
                        }
                    }
                    latch.countDown();
                }).start();
            }
            latch.await();
            count.set(0);
            CountDownLatch readLatch = new CountDownLatch(10);
            for (int k = 0; k < 10; k++){
                new Thread(() -> {
                    int i = 0;
                    for (String key : keys.keySet()) {
                        i++;
                        TreeLeaf find = tree.find(key);
                        if (find == null) {
                            System.out.println(treeId);
                            System.out.println(key);
                            throw new RuntimeException();
                        }
                        int i1 = count.incrementAndGet();
                        if (i1 % 10000 == 0) {
                            System.out.println("find " + i1 + " " + (System.currentTimeMillis() - time));
                        }
                    }
                    readLatch.countDown();
                }).start();
            }
            readLatch.await();
            System.out.println(System.currentTimeMillis() - time);
        } catch (Exception e) {
            tree.printTree();
            throw new CodeException(e);
        }
    }


    @Test
    public void testEdit() throws IOException {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                300, new TreeLineDeserialize(new FileBlockLeafDeserialize()), 40000,
                200, new RandomAccessFileMappingFactory(), false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        long treeId = idList.next();
        BPlusTree tree = new BPlusTree(treeId, idList, allocation);
        FileBlock test = SerializeTest.createLeaf("test");
        tree.add(test);
        test.setChildRootId(123);
        tree.edit(test);
        TreeLeaf leaf = tree.find(test.getName());
        Assert.assertEquals(leaf.getName(), test.getName());
        System.out.println(((FileBlock) leaf).getChildRootId());
    }

    @Test
    public void test() throws IOException, InterruptedException {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                300, new TreeLineDeserialize(new FileBlockLeafDeserialize()), 40000,
                200, new RandomAccessFileMappingFactory(), false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        long treeId = idList.next();
        BPlusTree index = new BPlusTree(treeId, idList, allocation);
        String name = "2020";
        CountDownLatch latch = new CountDownLatch(10);
        for(int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                FileBlock leaf = (FileBlock) index.find(name);
                if (leaf == null) {
                    leaf = new FileBlock();
                    leaf.setName(name);
                    leaf.setType(EnumFileType.DIRECTORY.getType());
                    leaf.setCreateTime(finalI);
                    index.addWithoutException(leaf);
                    leaf = (FileBlock) index.find(leaf.getName());
                    if (leaf == null) {
                        throw new CodeException();
                    }else {
                        System.out.println(leaf.getCreateTime());
                    }
                }else {
                    System.out.println(Thread.currentThread().getName());
                }
                latch.countDown();
            }).start();
        }
        latch.await();
    }

    @Test
    public void testDefaultLeaf() throws IOException
    {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                300, new TreeLineDeserialize(new DefaultByteLeafDeserialize()), 40000,
                200, new RandomAccessFileMappingFactory(), false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        long treeId = idList.next();
        BPlusTree tree = new BPlusTree(treeId, idList, allocation);
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++){
            tree.add(createLeaf(i + ""));
        }
        tree.printTree();
        tree.findWithPrefix("test", treeLeaf ->
        {
            System.out.println(treeLeaf.getName());
            return null;
        }, null);
        for (int i = 0; i < 10; i++){
            TreeLeaf leaf = tree.find(("" +  i + ""));
            if (leaf == null || !leaf.getName().equals("" +  i + "")){
                throw new IOException();
            }
        }
        AtomicInteger integer = new AtomicInteger(0);
        tree.foreach((leaf) -> {
            integer.getAndIncrement();
            return null;
        }, null);
        Assert.assertEquals(integer.get(), 10);
        System.out.println(System.currentTimeMillis() - time);
    }

    @Test
    public void testPrefix() throws IOException
    {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                1024, new TreeLineDeserialize(new DefaultByteLeafDeserialize()), 40000, 0, new RandomAccessFileMappingFactory(), false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        long treeId = idList.next();
        BPlusTree tree = new BPlusTree(treeId, idList, allocation);
        Random random = new Random();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++){
            String name = getRandomString(23);
            tree.add(createLeaf(name));
            if (i % 10000 == 0){
                System.out.println(i + " = " + (System.currentTimeMillis() - time));
            }
        }
        tree.add(createLeaf("test-hello"));
        for (int i = 0; i < 1000; i++){
            tree.add(createLeaf("test-hello-" + i));
        }
        time = System.currentTimeMillis();
        for (int i = 0; i < 1; i++)
        {

            AtomicInteger integer = new AtomicInteger();
            String lastName = null;
            while (integer.get() == 0 || lastName != null)
            {
                lastName = (String) tree.findWithPrefix("test", treeLeaf ->
                {
                    System.out.println(treeLeaf.getName());
                    int andIncrement = integer.getAndIncrement();
                    if (andIncrement > 12){
                        return treeLeaf.getName();
                    }
                    return null;
                }, lastName);
            }
        }
        System.out.println(System.currentTimeMillis() - time);
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static DefaultByteLeaf createLeaf(String name){
        DefaultByteLeaf value = new DefaultByteLeaf();
        value.setName(name);
        value.setIndex(value.getNameCodes());
        return value;
    }

    @Test
    public void testForeach() throws IOException
    {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                300, new TreeLineDeserialize(new DefaultByteLeafDeserialize()), 40000, 200, new RandomAccessFileMappingFactory(), false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        long treeId = idList.next();
        BPlusTree tree = new BPlusTree(treeId, idList, allocation);
        int size = 100000;
        Map<String, Boolean> map = new HashMap<>(size);
        long time = System.currentTimeMillis();
        for (int i = 0; i < size; i++){
            String name = "" + i;
            tree.add(createLeaf(name));
            map.put(name, true);
            if (i % 10000 == 0){
                System.out.println(i + " = " + (System.currentTimeMillis() - time));
            }
        }
        time = System.currentTimeMillis();
        AtomicInteger integer = new AtomicInteger();
        String lastName = null;
        while (integer.get() == 0 || lastName != null)
        {
            lastName = (String) tree.foreach((leaf) ->
            {
                map.remove(leaf.getName());
                int andIncrement = integer.getAndIncrement();
                if (andIncrement >= 100)
                {
                    return leaf.getName();
                }
                return null;
            }, lastName);
        }
        if (map.size() > 0){
            throw new CodeException();
        }
        System.out.println(System.currentTimeMillis() - time);
    }

    @Test
    public void testThreadTree() throws IOException, InterruptedException {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        DiskBlockAllocation<SerializeHeader, Serialize> allocation = new DiskBlockAllocation<>(
                tempDirectory,
                8 * 1024,
                new TreeLineDeserialize(new FileBlockLeafDeserialize()),
                40000, 20000,
                new MappedByteBufferMappingFactory(),
                false);
        LongList idList = new LongList(new File(tempDirectory, "ID.LIST"), false);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < 10; i++){
            new Thread(() -> {
                long treeId = idList.next();
                BPlusTree tree = new BPlusTree(treeId, idList, allocation);
//                Random random = new Random();
                long time = System.currentTimeMillis();
                for (int j = 1; j <= 100000; j++) {
                    String name = "test-" + j;
                    FileBlock leaf = SerializeTest.createLeaf(name, j);
//                    if (tree.find(leaf.getName()) != null){
//                        continue;
//                    }
                    tree.add(leaf);
                    int c = count.incrementAndGet();
                    if (c % 10000 == 0) {
                        System.out.println("save " + c + " " + (System.currentTimeMillis() - time));
                    }
//                    TreeLeaf leaf1 = tree.find(leaf.getName());
//                    Assert.assertNotNull(leaf1);
                }
                latch.countDown();
            }).start();
        }
        latch.await();
    }

}
