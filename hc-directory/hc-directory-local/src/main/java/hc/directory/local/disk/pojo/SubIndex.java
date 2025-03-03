package hc.directory.local.disk.pojo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import hc.directory.local.disk.mapping.Serialize;
import hc.utils.convert.DataTransformUtil;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubIndex implements Serialize
{

    /**
     * {@link hc.directory.local.disk.constants.EnumIndexType}
     */
    private byte indexType;

    private List<Entry> subs = new ArrayList<>();

    public SubIndex(Byte type){
        this.indexType = type;
    }

    @Override
    public int len()
    {
        int len = DataTransformUtil.BYTE_SIZE;
        for (Entry sub : subs)
        {
            len += sub.len();
        }
        return len;
    }

    @Override
    public void toBytes(byte[] array, int offset)
    {
        int ri = offset;
        array[ri] = indexType;
        ri++;
        for (Entry sub : subs)
        {
            sub.copy(array, ri);
            ri += sub.len();
        }
    }

    @Override
    public byte[] toBytes()
    {
        int len = len();
        byte[] array = new byte[len];
        toBytes(array, 0);
        return array;
    }

    public static SubIndex create(byte[] array, int offset, int len)
    {
        SubIndex subIndex = new SubIndex(array[0]);
        subIndex.setIndexType(array[0]);
        int index = offset + DataTransformUtil.BYTE_SIZE;;
        while (index < offset + len - 1)
        {
            Entry entry = Entry.create(array, index);
            index += entry.len();
            subIndex.getSubs().add(entry);
        }
        return subIndex;
    }

    @Data
    public static class Entry
    {

        public Entry(String name, long diskId)
        {
            this.name = name;
            this.diskId = diskId;
            this.len = name.getBytes(StandardCharsets.UTF_8).length;
        }

        private String name;

        private long diskId;

        private int len;

        int len()
        {
            return len + DataTransformUtil.INT_SIZE + DataTransformUtil.LONG_SIZE;
        }

        void copy(byte[] data, int offset)
        {
            int len = len();
            DataTransformUtil.intToBytes(len, data, offset);
            byte[] nameCode = name.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(nameCode, 0, data, offset + DataTransformUtil.INT_SIZE, nameCode.length);
            DataTransformUtil.longToByteArray(diskId, data, offset + nameCode.length + DataTransformUtil.INT_SIZE);
        }

        static Entry create(byte[] data, int offset)
        {
            int len = DataTransformUtil.bytesToInt(data, offset, DataTransformUtil.INT_SIZE);
            int nameLen = len - DataTransformUtil.INT_SIZE - DataTransformUtil.LONG_SIZE;
            byte[] nameCodes = new byte[nameLen];
            System.arraycopy(data, DataTransformUtil.INT_SIZE + offset, nameCodes, 0, nameLen);
            String name = new String(nameCodes, StandardCharsets.UTF_8);
            long diskId = DataTransformUtil.byteArrayToLong(data, offset + DataTransformUtil.INT_SIZE + nameLen, DataTransformUtil.LONG_SIZE);
            return new Entry(name, diskId);
        }
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat);
    }

}
