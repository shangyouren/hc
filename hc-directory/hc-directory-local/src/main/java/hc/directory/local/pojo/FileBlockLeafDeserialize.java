package hc.directory.local.pojo;

import hc.directory.local.collect.btree.TreeLeaf;
import hc.directory.local.collect.btree.TreeLeafDeserialize;

public class FileBlockLeafDeserialize implements TreeLeafDeserialize
{
    @Override
    public TreeLeaf create(byte[] bytes, int offset)
    {
        return FileBlock.create(bytes, offset);
    }
}
