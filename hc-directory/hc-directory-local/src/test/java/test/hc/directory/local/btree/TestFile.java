package test.hc.directory.local.btree;

import org.junit.Test;
import test.hc.directory.local.mapping.DiskBlockAllocationTest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFile
{

    @Test
    public void test() throws IOException, InterruptedException {
        File tempDirectory = DiskBlockAllocationTest.createTempDirectory();
        AtomicInteger count = new AtomicInteger(0);
        long time = System.currentTimeMillis();
        for (int x = 0; x < 10; x++) {
            new Thread(() -> {
                for (int i = 0; i < 1000; i++) {
                    int i1 = count.incrementAndGet();
                    File file = new File(tempDirectory, "test-" + i1);
                    file.mkdir();
                    if (i1 % 1000 == 0) {
                        System.out.println(i1 + " " + (System.currentTimeMillis() - time));
                    }
                }
            }).start();

        }
        Thread.sleep(100000);
    }

}
