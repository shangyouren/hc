package hc.directory.local.disk.collect.btree;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import hc.directory.local.disk.mapping.Serialize;
import hc.directory.local.disk.mapping.SerializeHeader;
import hc.utils.convert.DataTransformUtil;
import hc.utils.errors.ArrayDisplacementException;
import lombok.Data;

import java.nio.charset.StandardCharsets;

@Data
public class TreeLeaf implements Serialize, Node {

    public static final byte[] VERIFY_CODE = new byte[]{0, 1, 0, 1, 0, 1, 0, 1};

    private String name;

    private byte[] nameCodes;

    public void setName(String name){
        this.name = name;
        this.nameCodes = name.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes()
    {
        int len = len();
        byte[] bytes = new byte[len];
        toBytes(bytes, 0);
        return bytes;
    }

    @Override
    public int len()
    {
        int valueLen =  valueLen();
        return getFinalLen(valueLen);
    }

    protected int valueLen(){
        return DataTransformUtil.INT_SIZE + nameCodes.length;
    }

    public static int getFinalLen(int valueLen){
        return valueLen + VERIFY_CODE.length + DataTransformUtil.INT_SIZE;
    }

    public static boolean hit(byte[] bytes, int seek){
        for (int i = 0; i < VERIFY_CODE.length; i++){
            if (bytes[seek + i] != VERIFY_CODE[i]){
                return false;
            }
        }
        return true;
    }

    @Override
    public void toBytes(byte[] finalCodes, int offset)
    {
        int valueLen =  valueLen();
        int ri = offset;
        System.arraycopy(VERIFY_CODE, 0, finalCodes, ri, VERIFY_CODE.length);
        ri += VERIFY_CODE.length;
        DataTransformUtil.intToBytes(valueLen, finalCodes, ri);
        ri += DataTransformUtil.INT_SIZE;
        DataTransformUtil.intToBytes(nameCodes.length, finalCodes, ri);
        ri += DataTransformUtil.INT_SIZE;
        System.arraycopy(nameCodes, 0, finalCodes, ri, nameCodes.length);
    }

    protected int baseFinalLen(){
        return DataTransformUtil.INT_SIZE + nameCodes.length + VERIFY_CODE.length + DataTransformUtil.INT_SIZE;
    }

    public static Header createHeader(byte[] bytes, int seek, Header header){
        if (!hit(bytes, seek)){
            throw new ArrayDisplacementException(TreeLeaf.class.getCanonicalName(), "VERIFY_CODE", "seek is " + seek);
        }
        int ri = VERIFY_CODE.length;
        int valueLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
        ri += DataTransformUtil.INT_SIZE;
        int nameLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
        ri += DataTransformUtil.INT_SIZE;
        byte[] nameCodes = new byte[nameLen];
        System.arraycopy(bytes, seek + ri, nameCodes, 0, nameLen);
        String name = new String(nameCodes, StandardCharsets.UTF_8);
        header.setName(name);
        // 与len联动
        header.setBlockSize(getFinalLen(valueLen));
        return header;
    }

    public static int create(byte[] bytes, int seek, TreeLeaf leaf)
    {
        for (int i = 0; i < VERIFY_CODE.length; i++){
            if (bytes[seek + i] != VERIFY_CODE[i]){
                throw new ArrayDisplacementException(TreeLeaf.class.getCanonicalName(), "VERIFY_CODE", "seek is " + seek);
            }
        }
        int ri = VERIFY_CODE.length;
        ri += DataTransformUtil.INT_SIZE;
        int nameLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
        ri += DataTransformUtil.INT_SIZE;
        byte[] nameCodes = new byte[nameLen];
        System.arraycopy(bytes, seek + ri, nameCodes, 0, nameLen);
        leaf.name = new String(nameCodes, StandardCharsets.UTF_8);
        leaf.setNameCodes(nameCodes);
        ri+= nameLen;
        return ri;
    }

    @Override
    public String toString(){
        return JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat);
    }

    @Data
    public static class Header implements SerializeHeader, Node
    {

        private int blockSize;

        private String name;

    }

}
