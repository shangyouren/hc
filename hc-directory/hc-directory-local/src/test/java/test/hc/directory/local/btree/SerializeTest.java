package test.hc.directory.local.btree;

import hc.directory.local.disk.collect.btree.*;
import hc.directory.local.disk.constants.EnumFileType;
import hc.directory.local.disk.pojo.FileBlock;
import hc.directory.local.disk.pojo.FileBlockLeafDeserialize;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class SerializeTest {

    @Test
    public void testBTreeSerializer(){
        TreeLineDeserialize deserialize = new TreeLineDeserialize(new FileBlockLeafDeserialize());
        FileBlock leaf = createLeaf("test");
        byte[] leafBytes = leaf.toBytes();
        FileBlock newLeaf = (FileBlock) deserialize.create(leafBytes, 0);
        Assert.assertEquals(leaf.getName(), newLeaf.getName());
        Assert.assertEquals(leaf.getId(), newLeaf.getId());
        Assert.assertEquals(leaf.getChildRootId(), newLeaf.getChildRootId());
        Assert.assertEquals(leaf.getCreateTime(), newLeaf.getCreateTime());
        Assert.assertEquals(leaf.getPermission(), newLeaf.getPermission());
        Assert.assertEquals(leaf.getType(), newLeaf.getType());
        Assert.assertEquals(leaf.getChildSize(), newLeaf.getChildSize());
        if (leaf.getIndex() == null){
            Assert.assertNull(newLeaf.getIndex());
        }else {
            Assert.assertArrayEquals(leaf.getIndex(), newLeaf.getIndex());
        }

        TreeLine line = createLine("test", System.currentTimeMillis());
        byte[] bytes = line.toBytes();
        TreeLine newLine = (TreeLine) deserialize.create(bytes, 0);
        Assert.assertEquals(line.getName(), newLine.getName());
        Assert.assertEquals(line.getType(), newLine.getType());
        Assert.assertEquals(line.getPoint(), newLine.getPoint());

    }

    public static TreeLine createLine(String name, long point){
        TreeLine line = new TreeLine();
        line.setName(name);
        line.setPoint(point);
        line.setType(EnumFileType.NON_LEAF.getType());
        return line;
    }

    public static FileBlock createLeaf(String name){
        FileBlock leaf = new FileBlock();
        leaf.setId(System.currentTimeMillis());
        leaf.setName(name);
        leaf.setChildRootId(System.currentTimeMillis() + 1);
        leaf.setCreateTime(System.currentTimeMillis() + 2);
        leaf.setIndex("{\"user\" = \"shangyouren\"}".getBytes(StandardCharsets.UTF_8));
        return leaf;
    }

    public static FileBlock createLeaf(String name, long i){
        FileBlock leaf = new FileBlock();
        leaf.setId(i);
        leaf.setName(name);
        leaf.setChildRootId(System.currentTimeMillis() + 1);
        leaf.setCreateTime(System.currentTimeMillis() + 2);
        leaf.setIndex("{\"user\" = \"shangyouren\"}".getBytes(StandardCharsets.UTF_8));
        return leaf;
    }

    @Test
    public void testDefaultLeaf(){
        DefaultByteLeafDeserialize deserialize = new DefaultByteLeafDeserialize();
        DefaultByteLeaf leaf = new DefaultByteLeaf();
        leaf.setName("test-2");
        leaf.setIndex("hello-".getBytes(StandardCharsets.UTF_8));
        byte[] finalCodes = new byte[leaf.len()];
        leaf.toBytes(finalCodes, 0);
        DefaultByteLeaf copy = (DefaultByteLeaf) deserialize.create(finalCodes, 0);
        Assert.assertEquals(copy.getName(), leaf.getName());
        Assert.assertEquals(new String(copy.getIndex(), StandardCharsets.UTF_8), new String(leaf.getIndex(), StandardCharsets.UTF_8));
    }
}
