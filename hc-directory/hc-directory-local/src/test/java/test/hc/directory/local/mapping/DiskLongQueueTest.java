package test.hc.directory.local.mapping;

import hc.directory.local.disk.collect.queue.DiskLongQueue;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class DiskLongQueueTest {

    @Test
    public void run() throws IOException {
        File tempFile = File.createTempFile("test-disk.queue", "queue");
        DiskLongQueue queue = new DiskLongQueue(false, tempFile);
        long time = System.currentTimeMillis();
        for (int i = 1; i < 10000; i++){
            queue.put(i);
            if ((i + 1) % 10000 == 0){
                System.out.println((i + 1) + " " + (System.currentTimeMillis() - time));
            }
        }
        int i = 1;
        long popValue = queue.pop();
        while (popValue != -1){
            Assert.assertEquals(i, popValue);
            popValue = queue.pop();
            i++;
            if ((i + 1) % 10000 == 0){
                System.out.println((i + 1) + " " + (System.currentTimeMillis() - time));
            }
        }
    }
}
