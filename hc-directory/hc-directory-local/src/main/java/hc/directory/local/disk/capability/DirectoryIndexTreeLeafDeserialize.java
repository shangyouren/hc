package hc.directory.local.disk.capability;

import hc.directory.local.disk.collect.btree.DefaultByteLeaf;
import hc.directory.local.disk.collect.btree.TreeLeaf;
import hc.directory.local.disk.collect.btree.TreeLeafDeserialize;

public class DirectoryIndexTreeLeafDeserialize implements TreeLeafDeserialize
{
    @Override
    public TreeLeaf create(byte[] bytes, int offset)
    {
        return DirectoryIndexTreeLeaf.create(bytes, offset);
    }
}
