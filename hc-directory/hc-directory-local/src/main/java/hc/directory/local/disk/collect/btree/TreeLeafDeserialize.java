package hc.directory.local.disk.collect.btree;

public interface TreeLeafDeserialize
{

    TreeLeaf create(byte[] bytes, int offset);
}
