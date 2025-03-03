package hc.directory.local.disk.collect.btree;

public class DefaultByteLeafDeserialize implements TreeLeafDeserialize
{
    @Override
    public TreeLeaf create(byte[] bytes, int offset)
    {
        return DefaultByteLeaf.create(bytes, offset);
    }
}
