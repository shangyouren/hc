package hc.directory.local.disk.collect.btree;

import hc.directory.local.disk.mapping.Deserialize;
import hc.directory.local.disk.mapping.Serialize;
import hc.directory.local.disk.mapping.SerializeHeader;

public class TreeLineDeserialize implements Deserialize<SerializeHeader, Serialize>
{

    private final TreeLeafDeserialize leafDeserialize;

    public TreeLineDeserialize(TreeLeafDeserialize leafDeserialize)
    {
        this.leafDeserialize = leafDeserialize;
    }

    public TreeLineDeserialize()
    {
        this.leafDeserialize = new DefaultByteLeafDeserialize();
    }

    @Override
    public Serialize create(byte[] data, int offset)
    {
        if (TreeLine.hit(data, offset)) {
            return TreeLine.create(data, offset);
        }
        return leafDeserialize.create(data, offset);
    }

    @Override
    public SerializeHeader newHeaderInstance(byte[] data, int offset)
    {
        if (TreeLine.hit(data, offset)) {
            return new TreeLine();
        }
        return new TreeLeaf.Header();
    }

    @Override
    public SerializeHeader createHeader(byte[] data, int offset, SerializeHeader h)
    {
        if (TreeLine.hit(data, offset)) {
            return TreeLine.create(data, offset, (TreeLine) h);
        }
        return TreeLeaf.createHeader(data, offset, (TreeLeaf.Header) h);
    }
}
