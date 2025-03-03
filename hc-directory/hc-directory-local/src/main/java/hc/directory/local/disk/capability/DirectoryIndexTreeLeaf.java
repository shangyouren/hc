package hc.directory.local.disk.capability;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import hc.directory.local.disk.collect.btree.TreeLeaf;
import hc.utils.convert.DataTransformUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryIndexTreeLeaf extends TreeLeaf {

    private long id;

    @Override
    protected int valueLen(){
        return super.valueLen() + (DataTransformUtil.LONG_SIZE);
    }

    @Override
    public void toBytes(byte[] finalCodes, int offset)
    {
        int ri = offset;
        super.toBytes(finalCodes, offset);
        ri += super.baseFinalLen();
        DataTransformUtil.longToByteArray(id, finalCodes, ri);
    }

    public static DirectoryIndexTreeLeaf create(byte[] bytes, int seek)
    {
        int ri = VERIFY_CODE.length;
        int valueLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
        DirectoryIndexTreeLeaf leaf = new DirectoryIndexTreeLeaf();
        ri = TreeLeaf.create(bytes, seek, leaf);
        int indexLen = valueLen - DataTransformUtil.INT_SIZE - leaf.getNameCodes().length;
        if (indexLen > 0) {
            leaf.setId(DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE));
        }
        return leaf;
    }

    @Override
    public String toString(){
        return JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat);
    }

}
