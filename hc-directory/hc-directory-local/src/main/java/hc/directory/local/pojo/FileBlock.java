package hc.directory.local.pojo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import hc.directory.local.collect.btree.TreeLeaf;
import hc.utils.convert.DataTransformUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileBlock extends TreeLeaf
{

    private long permission;

    private long createTime;

    /**
     * 0为占位，1为文件夹，2为文件，3为软链接
     */
    private byte type;

    private long id;

    private long childRootId = -1;

    private byte[] index;

    private long childSize;

    @Override
    protected int valueLen(){
        return super.valueLen() + DataTransformUtil.LONG_SIZE +
                DataTransformUtil.LONG_SIZE + DataTransformUtil.LONG_SIZE + DataTransformUtil.LONG_SIZE
                + DataTransformUtil.BYTE_SIZE + (index == null ? 0 : index.length) + DataTransformUtil.LONG_SIZE;
    }

    @Override
    public void toBytes(byte[] finalCodes, int offset)
    {
        int ri = offset;
        super.toBytes(finalCodes, offset);
        ri += super.baseFinalLen();
        DataTransformUtil.longToByteArray(permission, finalCodes, ri);
        ri += DataTransformUtil.LONG_SIZE;
        DataTransformUtil.longToByteArray(createTime, finalCodes, ri);
        ri += DataTransformUtil.LONG_SIZE;
        DataTransformUtil.longToByteArray(id, finalCodes, ri);
        ri += DataTransformUtil.LONG_SIZE;
        DataTransformUtil.longToByteArray(childRootId, finalCodes, ri);
        ri += DataTransformUtil.LONG_SIZE;
        finalCodes[ri] = type;
        ri ++;
        if (index != null) {
            System.arraycopy(index, 0, finalCodes, ri, index.length);
            ri += index.length;
        }
        DataTransformUtil.longToByteArray(childSize, finalCodes, ri);
    }

    public static FileBlock create(byte[] bytes, int seek)
    {
        int ri = VERIFY_CODE.length;
        int valueLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
        FileBlock leaf = new FileBlock();
        ri = TreeLeaf.create(bytes, seek, leaf);
        long permission = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
        ri += DataTransformUtil.LONG_SIZE;
        long createTime = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
        ri += DataTransformUtil.LONG_SIZE;
        long id = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
        ri += DataTransformUtil.LONG_SIZE;
        long childRootId = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
        ri += DataTransformUtil.LONG_SIZE;
        byte type = bytes[seek + ri];
        ri ++;
        int indexLen = valueLen - DataTransformUtil.INT_SIZE -
                leaf.getNameCodes().length - DataTransformUtil.LONG_SIZE - DataTransformUtil.LONG_SIZE -
                DataTransformUtil.LONG_SIZE - DataTransformUtil.LONG_SIZE - DataTransformUtil.BYTE_SIZE - DataTransformUtil.LONG_SIZE;
        if (indexLen > 0) {
            byte[] index = new byte[indexLen];
            System.arraycopy(bytes, seek + ri, index, 0, indexLen);
            ri += indexLen;
            leaf.setIndex(index);
        }
        long childSize = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
        leaf.setId(id);
        leaf.setCreateTime(createTime);
        leaf.setChildRootId(childRootId);
        leaf.setPermission(permission);
        leaf.setType(type);
        leaf.setChildSize(childSize);
        return leaf;
    }

    @Override
    public String toString(){
        return JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat);
    }

}
