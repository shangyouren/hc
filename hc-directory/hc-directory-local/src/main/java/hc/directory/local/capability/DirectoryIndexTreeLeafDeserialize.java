package hc.directory.local.capability;

import hc.directory.local.collect.btree.TreeLeaf;
import hc.directory.local.collect.btree.TreeLeafDeserialize;

public class DirectoryIndexTreeLeafDeserialize implements TreeLeafDeserialize
{
    @Override
    public TreeLeaf create(byte[] bytes, int offset)
    {
        return DirectoryIndexTreeLeaf.create(bytes, offset);
    }
}
