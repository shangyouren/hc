package hc.directory.local.disk.collect.btree;

import hc.utils.errors.IOException;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

public class LongList {

    private final RandomAccessFile file;

    private final ReentrantLock lock;

    public LongList(File idFile, boolean syncInstantly){
        try {
            this.file = new RandomAccessFile(idFile, syncInstantly ? "rws" : "rw");
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
        this.lock = new ReentrantLock();
    }

    public long next(){
        lock.lock();
        try {
            long id = file.length();
            this.file.seek(id);
            this.file.writeLong(-1L);
            return id;
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

    public void sync(long id, long value){
        lock.lock();
        try {
            this.file.seek(id);
            this.file.writeLong(value);
        }catch (java.io.IOException e){
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

    public long read(long id){
        lock.lock();
        try {
            this.file.seek(id);
            return this.file.readLong();
        }catch (java.io.IOException e){
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

    public long fileSize(){
        lock.lock();
        try {
            return this.file.length();
        }catch (java.io.IOException e){
            throw new IOException(e);
        }finally {
            lock.unlock();
        }
    }

}
