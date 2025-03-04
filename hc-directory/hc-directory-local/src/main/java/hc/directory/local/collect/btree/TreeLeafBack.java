//package hc.directory.local.disk.collect.btree;
//
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONWriter;
//import hc.directory.local.disk.mapping.Serialize;
//import hc.directory.local.disk.mapping.SerializeHeader;
//import hc.utils.convert.DataTransformUtil;
//import hc.utils.errors.ArrayDisplacementException;
//import lombok.Data;
//
//import java.nio.charset.StandardCharsets;
//
//@Data
//public class TreeLeafBack implements Serialize, Node {
//
//    public static final byte[] VERIFY_CODE = new byte[]{0, 1, 0, 1, 0, 1, 0, 1};
//
//    private String name;
//
//    private byte[] nameCodes;
//
//    private long permission;
//
//    private long createTime;
//
//    /**
//     * 0为占位，1为文件夹，2为文件，3为软链接
//     */
//    private byte type;
//
//    private long id;
//
//    private long childRootId;
//
//    private byte[] index;
//
//    private long childSize;
//
//    public void setName(String name){
//        this.name = name;
//        this.nameCodes = name.getBytes(StandardCharsets.UTF_8);
//    }
//
//    @Override
//    public byte[] toBytes()
//    {
//        int len = len();
//        byte[] bytes = new byte[len];
//        toBytes(bytes, 0);
//        return bytes;
//    }
//
//    @Override
//    public int len()
//    {
//        int valueLen =  valueLen();
//        return getFinalLen(valueLen);
//    }
//
//    private int valueLen(){
//        return DataTransformUtil.INT_SIZE + nameCodes.length + DataTransformUtil.LONG_SIZE +
//                DataTransformUtil.LONG_SIZE + DataTransformUtil.LONG_SIZE + DataTransformUtil.LONG_SIZE
//                + DataTransformUtil.BYTE_SIZE + (index == null ? 0 : index.length) + DataTransformUtil.LONG_SIZE;
//    }
//
//    public static int getFinalLen(int valueLen){
//        return valueLen + VERIFY_CODE.length + DataTransformUtil.INT_SIZE;
//    }
//
//    public static boolean hit(byte[] bytes, int seek){
//        for (int i = 0; i < VERIFY_CODE.length; i++){
//            if (bytes[seek + i] != VERIFY_CODE[i]){
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void toBytes(byte[] finalCodes, int offset)
//    {
//        int valueLen =  valueLen();
//
//        int ri = offset;
//        System.arraycopy(VERIFY_CODE, 0, finalCodes, ri, VERIFY_CODE.length);
//        ri += VERIFY_CODE.length;
//        DataTransformUtil.intToBytes(valueLen, finalCodes, ri);
//        ri += DataTransformUtil.INT_SIZE;
//        DataTransformUtil.intToBytes(nameCodes.length, finalCodes, ri);
//        ri += DataTransformUtil.INT_SIZE;
//        System.arraycopy(nameCodes, 0, finalCodes, ri, nameCodes.length);
//        ri += nameCodes.length;
//        DataTransformUtil.longToByteArray(permission, finalCodes, ri);
//        ri += DataTransformUtil.LONG_SIZE;
//        DataTransformUtil.longToByteArray(createTime, finalCodes, ri);
//        ri += DataTransformUtil.LONG_SIZE;
//        DataTransformUtil.longToByteArray(id, finalCodes, ri);
//        ri += DataTransformUtil.LONG_SIZE;
//        DataTransformUtil.longToByteArray(childRootId, finalCodes, ri);
//        ri += DataTransformUtil.LONG_SIZE;
//        finalCodes[ri] = type;
//        ri ++;
//        if (index != null) {
//            System.arraycopy(index, 0, finalCodes, ri, index.length);
//            ri += index.length;
//        }
//        DataTransformUtil.longToByteArray(childSize, finalCodes, ri);
//    }
//
//    public static Header createHeader(byte[] bytes, int seek, Header header){
//        for (int i = 0; i < VERIFY_CODE.length; i++){
//            if (bytes[seek + i] != VERIFY_CODE[i]){
//                throw new ArrayDisplacementException(TreeLeafBack.class.getCanonicalName(), "VERIFY_CODE", "seek is " + seek);
//            }
//        }
//        int ri = VERIFY_CODE.length;
//        int valueLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
//        ri += DataTransformUtil.INT_SIZE;
//        int nameLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
//        ri += DataTransformUtil.INT_SIZE;
//        byte[] nameCodes = new byte[nameLen];
//        System.arraycopy(bytes, seek + ri, nameCodes, 0, nameLen);
//        ri = ri + nameLen + (DataTransformUtil.LONG_SIZE * 3);
//        long parentId = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
//        String name = new String(nameCodes, StandardCharsets.UTF_8);
//        header.setName(name);
//        // 与len联动
//        header.setBlockSize(getFinalLen(valueLen));
//        header.setParent(parentId);
//        return header;
//    }
//
//    public static TreeLeafBack create(byte[] bytes, int seek)
//    {
//        for (int i = 0; i < VERIFY_CODE.length; i++){
//            if (bytes[seek + i] != VERIFY_CODE[i]){
//                throw new ArrayDisplacementException(TreeLeafBack.class.getCanonicalName(), "VERIFY_CODE", "seek is " + seek);
//            }
//        }
//        int ri = VERIFY_CODE.length;
//        int valueLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
//        ri += DataTransformUtil.INT_SIZE;
//        int nameLen = DataTransformUtil.bytesToInt(bytes, seek + ri, DataTransformUtil.INT_SIZE);
//        ri += DataTransformUtil.INT_SIZE;
//        byte[] nameCodes = new byte[nameLen];
//        System.arraycopy(bytes, seek + ri, nameCodes, 0, nameLen);
//        ri += nameLen;
//        String name = new String(nameCodes, StandardCharsets.UTF_8);
//        long permission = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
//        ri += DataTransformUtil.LONG_SIZE;
//        long createTime = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
//        ri += DataTransformUtil.LONG_SIZE;
//        long id = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
//        ri += DataTransformUtil.LONG_SIZE;
//        long childRootId = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
//        ri += DataTransformUtil.LONG_SIZE;
//        byte type = bytes[seek + ri];
//        ri ++;
//        int indexLen = valueLen - DataTransformUtil.INT_SIZE -
//                nameLen - DataTransformUtil.LONG_SIZE - DataTransformUtil.LONG_SIZE -
//                DataTransformUtil.LONG_SIZE - DataTransformUtil.LONG_SIZE - DataTransformUtil.BYTE_SIZE - DataTransformUtil.LONG_SIZE;
//        TreeLeafBack fileBlock = new TreeLeafBack();
//        if (indexLen > 0) {
//            byte[] index = new byte[indexLen];
//            System.arraycopy(bytes, seek + ri, index, 0, indexLen);
//            ri += indexLen;
//            fileBlock.setIndex(index);
//        }
//        long childSize = DataTransformUtil.byteArrayToLong(bytes, seek + ri, DataTransformUtil.LONG_SIZE);
//        fileBlock.setId(id);
//        fileBlock.setCreateTime(createTime);
//        fileBlock.name = name;
//        fileBlock.setNameCodes(nameCodes);
//        fileBlock.setChildRootId(childRootId);
//        fileBlock.setPermission(permission);
//        fileBlock.setType(type);
//        fileBlock.setChildSize(childSize);
//        return fileBlock;
//    }
//
//    @Override
//    public String toString(){
//        return JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat);
//    }
//
//    @Data
//    public static class Header implements SerializeHeader, Node
//    {
//
//        private int blockSize;
//
//        private String name;
//
//        private long parent;
//
//    }
//
//}
