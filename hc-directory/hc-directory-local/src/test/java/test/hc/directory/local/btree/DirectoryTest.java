package test.hc.directory.local.btree;

import cn.hutool.core.collection.ListUtil;
import hc.directory.local.disk.capability.DirectoryService;
import hc.directory.local.disk.capability.DirectoryServiceImpl;
import hc.directory.local.disk.capability.DirectoryServiceImplWithIndex;
import hc.directory.local.disk.constants.EnumFileType;
import hc.directory.local.disk.mapping.MappedByteBufferMappingFactory;
import hc.directory.local.disk.mapping.RandomAccessFileMappingFactory;
import hc.directory.local.disk.pojo.FileBlock;
import hc.directory.local.disk.pojo.Paged;
import hc.directory.local.disk.pojo.Path;
import hc.utils.convert.ProjectUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryTest {

    @Test
    public void test() throws InterruptedException {
        File baseDir = ProjectUtils.directoryPathWithCheck("temp-dir");
        DirectoryService directoryService = new DirectoryServiceImplWithIndex(
                baseDir, 8 * 1024, 8 * 1024, 40000, 20000,
                new MappedByteBufferMappingFactory(), false);
        testFind(testWrite(directoryService), directoryService);
        long time = System.currentTimeMillis();
        scan(directoryService, Path.ROOT, new LinkedList<>());
        System.out.println(System.currentTimeMillis() - time);
    }

    @Test
    public void testBigDec(){
        File baseDir = ProjectUtils.directoryPathWithCheck("temp-dir");
        DirectoryService directoryService = new DirectoryServiceImplWithIndex(
                baseDir, 8 * 1024, 8 * 1024, 40000, 20000,
                new MappedByteBufferMappingFactory(), false);

    }

    public void scan(DirectoryService directoryService, Path path, List<FileBlock> r){
        Paged<FileBlock> list = directoryService.list(path, null);
        for (FileBlock datum : list.getData()) {
            if (datum.getType() == EnumFileType.DIRECTORY.getType()) {
                scan(directoryService, new Path(path, datum.getName()), r);
            }else {
                r.add(datum);
            }
        }
    }

    private List<File> testWrite(DirectoryService directoryService) throws InterruptedException {
        File testDir = new File("D:\\");
        long time = System.currentTimeMillis();
        List<File> files = new ArrayList<>();
        scanFile(files, testDir);
        System.out.println("scan time: " + (System.currentTimeMillis() - time));
        files.remove(0);
        files = new HashSet<>(files).stream().toList();
        List<List<File>> split = ListUtil.split(files, 10000);
        time = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(split.size());
        AtomicInteger count = new AtomicInteger(0);
        for (List<File> fileList : split) {
            long finalTime = time;
            new Thread(() -> {
                for (File file : fileList) {
                    Path path = toPath(file);
                    FileBlock block = new FileBlock();
                    block.setName(path.getName());
                    block.setType(file.isDirectory() ? EnumFileType.DIRECTORY.getType() : EnumFileType.FILE.getType());
                    directoryService.addWithMakeDirs(block, path);
                    int i = count.incrementAndGet();
                    if (i % 10000 == 0){
                        System.out.println("write " + i + " " + (System.currentTimeMillis() - finalTime));
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        return files;
    }

    private void testFind(List<File> files, DirectoryService directoryService) throws InterruptedException {
        AtomicInteger c = new AtomicInteger(0);
        long time = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++){
            new Thread(() -> {
                Random random = new Random();
                for (int j = 0; j < 10000; j++){
                    FileBlock fetch = directoryService.fetch(toPath(files.get(random.nextInt(0, files.size()))));
                    Assert.assertNotNull(fetch);
                    int i1 = c.incrementAndGet();
                    if (i1 % 10000 == 0){
                        System.out.println(i1 + " " + (System.currentTimeMillis() - time));
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
    }

    private Path toPath(File file){
        return new Path(file.toString().replace("D:", "").replace("\\", "/"));
    }

    private void scanFile(List<File> values, File dir){
        File[] files = dir.listFiles();
        if (files == null){
            values.add(dir);
            return ;
        }
        for (File file : files) {
            scanFile(values, file);
        }
    }
}
