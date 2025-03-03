package hc.directory.local.disk.collect.queue;

import hc.directory.local.disk.mapping.Deserialize;
import hc.directory.local.disk.mapping.DiskBlock;
import hc.directory.local.disk.mapping.Serialize;
import hc.utils.convert.DataTransformUtil;
import hc.utils.convert.ProjectUtils;
import hc.utils.errors.ArrayDisplacementException;
import hc.utils.errors.FileLengthException;
import hc.utils.errors.IOException;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

public class DiskQueueLog<T extends Serialize>
{

    public static final byte[] VERIFY_CODE = new byte[]{1, 2, 0, 1, 0, 1, 0, 2};

    public DiskQueueLog(File file, Deserialize<?, T> deserialize, int valueMaxSize){
        this.valueMaxSize = valueMaxSize;
        this.deserialize = deserialize;
        ProjectUtils.checkFile(file);
        byte[] bytes = new byte[16];
        try {
            this.file = new RandomAccessFile(file, "rws");
            this.file.seek(0);
            if (file.length() > 0) {
                this.file.read(bytes);
                for (int i = 0; i < VERIFY_CODE.length; i++) {
                    if (bytes[i] != VERIFY_CODE[i]) {
                        throw new ArrayDisplacementException(DiskBlock.class.getCanonicalName(), "VERIFY_CODE", "");
                    }
                }
                this.point = DataTransformUtil.byteArrayToLong(bytes, 8, 8);
            }else {
                System.arraycopy(VERIFY_CODE, 0, bytes, 0, VERIFY_CODE.length);
                DataTransformUtil.longToByteArray(16, bytes, 8);
                this.point = 16;
                this.file.seek(0);
                this.file.write(bytes);
            }
        }catch (java.io.IOException e){
            throw new IOException(e);
        }
    }

    private final RandomAccessFile file;

    private final Deserialize<?, T> deserialize;

    private final int valueMaxSize;

    private long point;

    private final ReentrantLock lock = new ReentrantLock();

    public void put(T value){
        lock.lock();
        try {
            if (value.len() > valueMaxSize){
                throw new FileLengthException(value.len());
            }
            this.file.seek(file.length());
            byte[] bytes = new byte[value.len()];
            value.toBytes(bytes, 0);
            this.file.write(bytes);
        }catch (java.io.IOException e){
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

    public T pop(){
        lock.lock();
        try {
            if (this.point >= file.length()){
                return null;
            }
            this.file.seek(point);
            byte[] bytes = new byte[valueMaxSize];
            this.file.read(bytes);
            T value = deserialize.create(bytes, 0);
            this.point += value.len();
            this.file.seek(8);
            byte[] pointBytes = new byte[8];
            DataTransformUtil.longToByteArray(this.point, pointBytes, 0);
            this.file.write(pointBytes);
            return value;
        }catch (java.io.IOException e){
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

}
