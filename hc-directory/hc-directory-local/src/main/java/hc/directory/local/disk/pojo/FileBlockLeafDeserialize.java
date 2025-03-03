package hc.directory.local.disk.pojo;

import hc.directory.local.disk.collect.btree.TreeLeaf;
import hc.directory.local.disk.collect.btree.TreeLeafDeserialize;

public class FileBlockLeafDeserialize implements TreeLeafDeserialize
{
    @Override
    public TreeLeaf create(byte[] bytes, int offset)
    {
        return FileBlock.create(bytes, offset);
    }
}
