package hc.directory.local.mapping;

import hc.utils.convert.DataTransformUtil;
import hc.utils.errors.ArrayDisplacementException;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Data
public class DiskBlock<H extends SerializeHeader, S extends Serialize> {

    private long id;

    private int size;

    private int valueLen;

    private byte[] values;

    private final DiskMapping map;

    private final Deserialize<H, S> deserialize;

    public static final byte[] VERIFY_CODE = new byte[]{0, 2, 0, 1, 0, 1, 0, 2};

    public DiskBlock(
            DiskMapping map, long id, int size,
            Deserialize<H, S> deserialize
    ) {
        this.deserialize = deserialize;
        this.map = map;
        this.id = id;
        this.size = size;
        this.values = new byte[size];
        map.get(values);
        for (int i = 0; i < VERIFY_CODE.length; i++) {
            if (values[i] != VERIFY_CODE[i]) {
                for (int j = 0; j < VERIFY_CODE.length; j++) {
                    if (values[j] != 0) {
                        throw new ArrayDisplacementException(DiskBlock.class.getCanonicalName(), "VERIFY_CODE", "id is " + id);
                    }
                }
            }
        }
        this.valueLen = valueLenFromValues();
    }

    public Object headerForeach(BiFunction<H, Integer, Object> consumer) {
        int offset = DataTransformUtil.INT_SIZE * 2 + DataTransformUtil.LONG_SIZE + VERIFY_CODE.length;
        H headerInstance = deserialize.newHeaderInstance(values, offset);
        while (offset < (valueLen + metadataLen()) - 1) {
            H header = deserialize.createHeader(values, offset, headerInstance);
            int blockSize = header.getBlockSize();
            Object next = consumer.apply(header, offset);
            if ((next != null)) {
                return next;
            }
            offset += blockSize;
        }
        return null;
    }

    public S read(int offset) {
        return deserialize.create(values, offset);
    }

    public int metadataLen() {
        return DataTransformUtil.INT_SIZE * 2 + DataTransformUtil.LONG_SIZE + VERIFY_CODE.length;
    }

    public Object foreach(BiFunction<S, Integer, Object> consumer) {
        int offset = metadataLen();
        while (offset < (valueLen + metadataLen()) - 1) {
            S block = deserialize.create(values, offset);
            int blockSize = block.len();
            Object next = consumer.apply(block, offset);
            if ((next != null)) {
                return next;
            }
            offset += blockSize;
        }
        return null;
    }

    public List<S> values() {
        List<S> values = new ArrayList<>();
        foreach((data, offset) -> {
            values.add(data);
            return null;
        });
        return values;
    }

    public boolean edit(S file, int offset) {
        S ori = deserialize.create(values, offset);
        if (emptySize() < file.len() - ori.len()) {
            return false;
        }
        byte[] bytes = new byte[valueLen + metadataLen() - offset - ori.len()];
        System.arraycopy(values, offset + ori.len(), bytes, 0, bytes.length);
        System.arraycopy(bytes, 0, values, offset, bytes.length);
        int newOffset = offset + bytes.length;
        file.toBytes(values, newOffset);
        valueLen = offset + bytes.length + file.len() - metadataLen();
        sync();
        return true;
    }

    public boolean add(S file) {
        int emptySize = emptySize();
        int fileLen = file.len();
        if (emptySize < fileLen) {
            return false;
        }
        int position = valueLen + metadataLen();
        file.toBytes(values, position);
        valueLen += file.len();
        sync();
        return true;
    }

    public boolean addAll(List<S> list) {
        int len = 0;
        for (S s : list) {
            len += s.len();
        }
        if (size - metadataLen() < len) {
            return false;
        }
        for (S file : list) {
            int position = valueLen + metadataLen();
            file.toBytes(values, position);
            valueLen += file.len();
        }
        sync();
        return true;
    }

    public void clear() {
        valueLen = 0;
        Arrays.fill(values, (byte) 0);
        sync();
    }

    public boolean clearAndSet(List<S> list) {
        int len = 0;
        for (S s : list) {
            len += s.len();
        }
        if (size - metadataLen() < len) {
            return false;
        }
        this.valueLen = 0;
        Arrays.fill(values, metadataLen(), values.length, (byte) 0);
        addAll(list);
        return true;
    }

    public void delete(int offset) {
        deleteWithoutSync(offset);
        sync();
    }

    private void deleteWithoutSync(int offset) {
        S ori = deserialize.create(values, offset);
        byte[] bytes = new byte[valueLen + metadataLen() - offset - ori.len()];
        System.arraycopy(values, offset + ori.len(), bytes, 0, bytes.length);
        System.arraycopy(bytes, 0, values, offset, bytes.length);
        valueLen = offset + bytes.length - metadataLen();
    }


    public void batchDelete(Map<Integer, S> files) {
        files.forEach((offset, file) -> deleteWithoutSync(offset));
        sync();
    }

    public void metadataSync() {
        System.arraycopy(VERIFY_CODE, 0, values, 0, VERIFY_CODE.length);
        DataTransformUtil.longToByteArray(id, values, VERIFY_CODE.length);
        DataTransformUtil.intToBytes(size, values, DataTransformUtil.LONG_SIZE + VERIFY_CODE.length);
        DataTransformUtil.intToBytes(valueLen, values, DataTransformUtil.LONG_SIZE + DataTransformUtil.INT_SIZE + VERIFY_CODE.length);
    }

    public int valueLenFromValues() {
        return DataTransformUtil.bytesToInt(values, DataTransformUtil.LONG_SIZE + DataTransformUtil.INT_SIZE + VERIFY_CODE.length, DataTransformUtil.INT_SIZE);
    }

    public void close() {
        sync();
        this.map.force();
    }


    public int emptySize() {
        return size - valueLen - metadataLen();
    }

    public long id() {
        return id;
    }

    public void sync() {
        metadataSync();
        this.map.position(0);
        this.map.put(values);
    }


    @Data
    public static class Point<T> {

        private final T data;

        private final long offset;

    }
}
