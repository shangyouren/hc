package hc.directory.local.collect.btree;

public interface TreeLeafDeserialize
{

    TreeLeaf create(byte[] bytes, int offset);
}
