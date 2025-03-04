package test.hc.directory.local.btree;

import hc.directory.local.collect.btree.LongList;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IdListTest {

    @Test
    public void test() throws IOException, InterruptedException {
        File tempFile = File.createTempFile("id-test-" + System.currentTimeMillis(), ".list");
        LongList longList = new LongList(tempFile, false);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            long oneId = longList.next();
            ids.add(oneId);
        }
        long read = longList.read(ids.get(0));
        Assert.assertEquals(read, -1);
        AtomicInteger count = new AtomicInteger();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++){
            int finalI = i;
            new Thread(() -> {
                long id = ids.get(finalI);
                for (int j = 0; j < 1000; j++){
                    longList.sync(id, j);
                    int i1 = count.incrementAndGet();
                    if (i1 % 10000 == 0){
                        System.out.println(i1 + " " + (System.currentTimeMillis() - time));
                    }
                }
                Assert.assertEquals(longList.read(id), 1000 - 1);
            }).start();
        }
    }
}
