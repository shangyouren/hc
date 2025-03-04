//package hc.directory.local.disk.capability;
//
//import hc.directory.local.disk.collect.btree.BPlusTree;
//import hc.directory.local.disk.collect.btree.RootMapping;
//import hc.directory.local.disk.mapping.DiskBlockAllocation;
//import hc.directory.local.disk.mapping.Serialize;
//import hc.directory.local.disk.mapping.SerializeHeader;
//import hc.directory.local.disk.pojo.Path;
//import lombok.Getter;
//
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//@Getter
//public class DirectoryIndex extends BPlusTree {
//
//    private final Path path;
//    public DirectoryIndex(Path path, RootMapping rootMapping, DiskBlockAllocation<SerializeHeader, Serialize> allocation, ReentrantReadWriteLock lock) {
//        super(rootMapping, allocation, lock);
//        this.path = path;
//    }
//}
