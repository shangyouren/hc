package hc.directory.local.collect.btree;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import hc.utils.convert.DataTransformUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultByteLeaf extends TreeLeaf
{

    private byte[] index;

    @Override
    protected int valueLen(){
        return super.valueLen() + (index == null ? 0 : index.length);
    }

    @Override
    public void toBytes(byte[] finalCodes, int offset)
    {
        int ri = offset;
        super.toBytes(finalCodes, offset);
        ri += super.baseFinalLen();
        if (index != null) {
            System.arraycopy(index, 0, finalCodes, ri, index.length);
        }
    }

    public static DefaultByteLeaf create(byte[] bytes, int seek)
    {
        int ri = VERIFY_CODE.length;
        int valueLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
        DefaultByteLeaf leaf = new DefaultByteLeaf();
        ri = create(bytes, seek, leaf);
        int indexLen = valueLen - DataTransformUtil.INT_SIZE - leaf.getNameCodes().length;
        if (indexLen > 0) {
            byte[] index = new byte[indexLen];
            System.arraycopy(bytes, seek + ri, index, 0, indexLen);
            leaf.setIndex(index);
        }
        return leaf;
    }

    @Override
    public String toString(){
        return JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat);
    }

}
