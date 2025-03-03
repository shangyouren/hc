package hc.directory.local.disk.collect.btree;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import hc.directory.local.disk.mapping.Serialize;
import hc.directory.local.disk.mapping.SerializeHeader;
import hc.utils.convert.DataTransformUtil;
import hc.utils.errors.ArrayDisplacementException;

import java.nio.charset.StandardCharsets;

public class TreeLine implements Serialize, SerializeHeader, Node
{

    public static final byte[] VERIFY_CODE = new byte[]{0, 1, 0, 1, 0, 1, 3, 3};


    private String name;

    private byte[] nameCodes;

    private long point;

    private byte type;

    @Override
    public String getName()
    {
        return name;
    }

    public long getPoint()
    {
        return point;
    }

    public void setPoint(long point)
    {
        this.point = point;
    }

    public byte getType()
    {
        return type;
    }

    public void setType(byte type)
    {
        this.type = type;
    }

    public void setName(String name){
        this.name = name;
        this.nameCodes = name.getBytes(StandardCharsets.UTF_8);
    }

    private void setNameOnly(String name){
        this.name = name;
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
    public byte[] toBytes()
    {
        byte[] finalCodes = new byte[len()];
        toBytes(finalCodes, 0);
        return finalCodes;
    }

    @Override
    public int len()
    {
        return valueLen() + VERIFY_CODE.length;
    }

    private int valueLen(){
        return DataTransformUtil.INT_SIZE + nameCodes.length + DataTransformUtil.LONG_SIZE
                + DataTransformUtil.BYTE_SIZE ;
    }

    @Override
    public void toBytes(byte[] finalCodes, int offset)
    {
        int ri = offset;
        System.arraycopy(VERIFY_CODE, 0, finalCodes, ri, VERIFY_CODE.length);
        ri += VERIFY_CODE.length;
        DataTransformUtil.intToBytes(nameCodes.length, finalCodes, ri);
        ri += DataTransformUtil.INT_SIZE;
        System.arraycopy(nameCodes, 0, finalCodes, ri, nameCodes.length);
        ri += nameCodes.length;
        DataTransformUtil.longToByteArray(point, finalCodes, ri);
        ri += DataTransformUtil.LONG_SIZE;
        finalCodes[ri] = type;
    }

    public static TreeLine create(byte[] bytes, int seek){
        return create(bytes, seek, new TreeLine());
    }

    public static TreeLine create(byte[] bytes, int seek, TreeLine treeLine){
        for (int i = 0; i < VERIFY_CODE.length; i++){
            if (bytes[seek + i] != VERIFY_CODE[i]){
                throw new ArrayDisplacementException(TreeLine.class.getCanonicalName(), "VERIFY_CODE", "seek is " + seek);
            }
        }
        int ri = VERIFY_CODE.length;
        int nameLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
        ri += DataTransformUtil.INT_SIZE;
        byte[] nameCodes = new byte[nameLen];
        System.arraycopy(bytes, seek + ri, nameCodes, 0, nameLen);
        ri += nameLen;
        String name = new String(nameCodes, StandardCharsets.UTF_8);
        long point = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
        ri += DataTransformUtil.LONG_SIZE;
        byte type = bytes[seek + ri];
        treeLine.setNameOnly(name);
        treeLine.nameCodes = nameCodes;
        treeLine.setType(type);
        treeLine.setPoint(point);
        return treeLine;
    }

    @Override
    public int getBlockSize()
    {
        return len();
    }

    @Override
    public String toString(){
        return JSON.toJSONString(this,
                JSONWriter.Feature.PrettyFormat
        );
    }
}
