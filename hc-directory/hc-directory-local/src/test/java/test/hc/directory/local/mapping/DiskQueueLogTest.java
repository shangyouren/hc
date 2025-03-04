package test.hc.directory.local.mapping;

import hc.directory.local.collect.btree.TreeLeaf;
import hc.directory.local.collect.btree.TreeLineDeserialize;
import hc.directory.local.collect.queue.DiskQueueLog;
import hc.directory.local.mapping.Serialize;
import hc.directory.local.pojo.FileBlockLeafDeserialize;
import hc.utils.errors.CodeException;
import org.junit.Test;
import test.hc.directory.local.btree.SerializeTest;

import java.io.File;
import java.io.IOException;

public class DiskQueueLogTest
{

    @Test
    public void test() throws IOException
    {
        DiskQueueLog<Serialize> test = new DiskQueueLog<>(
                File.createTempFile("test", ".txt")
                , new TreeLineDeserialize(new FileBlockLeafDeserialize()), 400);
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++){
            TreeLeaf leaf = SerializeTest.createLeaf("test-sldkfjsdf-" + i);
            test.put(leaf);
            if ((i + 1) % 10000 == 0){
                System.out.println((i + 1) + " " + (System.currentTimeMillis() - time));
            }
        }

        for (int i = 0; i < 10000000; i++){
            String name = "test-sldkfjsdf-" + i;
            TreeLeaf leaf = (TreeLeaf) test.pop();
            if (!name.equals(leaf.getName())){
                throw new CodeException();
            }
            if ((i + 1) % 10000 == 0){
                System.out.println((i + 1) + " " + (System.currentTimeMillis() - time));
            }
        }
    }

}
