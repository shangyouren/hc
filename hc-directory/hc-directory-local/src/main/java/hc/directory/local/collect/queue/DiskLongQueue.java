package hc.directory.local.collect.queue;

import hc.directory.local.mapping.DiskBlock;
import hc.utils.convert.DataTransformUtil;
import hc.utils.convert.ProjectUtils;
import hc.utils.errors.ArrayDisplacementException;
import hc.utils.errors.CodeException;
import hc.utils.errors.IOException;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

public class DiskLongQueue {

    public static final byte[] VERIFY_CODE = new byte[]{1, 2, 0, 1, 0, 1, 0, 1};

    public DiskLongQueue(boolean syncInstantly, File file){
        ProjectUtils.checkFile(file);
        if (file.length() % 8 != 0){
            throw new CodeException("File check error");
        }
        byte[] bytes = new byte[16];
        try {
            this.file = new RandomAccessFile(file, syncInstantly ? "rws" : "rw");
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

    private long point;

    private final ReentrantLock lock = new ReentrantLock();

    public void put(long value){
        lock.lock();
        try {
            this.file.seek(file.length());
            byte[] bytes = new byte[8];
            DataTransformUtil.longToByteArray(value, bytes, 0);
            this.file.write(bytes);
        }catch (java.io.IOException e){
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

    public long pop(){
        lock.lock();
        try {
            if (this.point >= file.length()){
                return -1L;
            }
            this.file.seek(point);
            byte[] bytes = new byte[8];
            this.file.read(bytes);
            this.point += 8;
            this.file.seek(8);
            byte[] pointBytes = new byte[8];
            DataTransformUtil.longToByteArray(this.point, pointBytes, 0);
            this.file.write(pointBytes);
            return DataTransformUtil.byteArrayToLong(bytes, 0, 8);
        }catch (java.io.IOException e){
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

}
