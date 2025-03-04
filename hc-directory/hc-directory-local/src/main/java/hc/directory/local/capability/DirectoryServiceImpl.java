package hc.directory.local.capability;

import hc.directory.local.collect.btree.BPlusTree;
import hc.directory.local.collect.btree.LongList;
import hc.directory.local.collect.btree.TreeLineDeserialize;
import hc.directory.local.constants.EnumFileType;
import hc.directory.local.mapping.DiskBlockAllocation;
import hc.directory.local.mapping.DiskMappingFactory;
import hc.directory.local.mapping.Serialize;
import hc.directory.local.mapping.SerializeHeader;
import hc.directory.local.pojo.FileBlock;
import hc.directory.local.pojo.FileBlockLeafDeserialize;
import hc.directory.local.pojo.Paged;
import hc.directory.local.pojo.Path;
import hc.utils.errors.CodeException;
import hc.utils.errors.FileAlreadyExistsException;
import hc.utils.errors.FileNotFoundException;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class DirectoryServiceImpl implements DirectoryService
{

    private final DiskBlockAllocation<SerializeHeader, Serialize> allocation;

    private final BPlusTree rootIndex;

    private final LongList idList;

    public DirectoryServiceImpl(
            File baseDir,
            int blockSize,
            int blockNumSize,
            int cacheSize,
            DiskMappingFactory mappingFactory,
            boolean releaseQueueSyncInstantly
    ){
        this.allocation = new DiskBlockAllocation<>(
                baseDir, blockSize,
                new TreeLineDeserialize(new FileBlockLeafDeserialize()),
                blockNumSize, cacheSize,
                mappingFactory, releaseQueueSyncInstantly
        );
        idList = new LongList(new File(baseDir, "ID.LIST"), releaseQueueSyncInstantly);
        if (idList.fileSize() <= 0){
            long next = idList.next();
            if (next != 0){
                throw new CodeException();
            }
        }
        this.rootIndex = allocation(Path.ROOT, 0, idList, allocation);
    }

    public void add(FileBlock block, Path path){
        block.setCreateTime(System.currentTimeMillis());
        BPlusTree index = findIndexWithPath(path.getParent());
        index.addIfAbsent(block, k -> {
            if (((FileBlock) k).getType() == EnumFileType.DIRECTORY.getType()) {
                ((FileBlock) k).setChildRootId(idList.next());
            }
        });
        release(index);
    }

    public void addWithMakeDirs(FileBlock block, Path path){
        block.setCreateTime(System.currentTimeMillis());
        BPlusTree index = makeDirs(path.getParent());
        index.addIfAbsent(block, k -> {
            if (((FileBlock) k).getType() == EnumFileType.DIRECTORY.getType()) {
                ((FileBlock) k).setChildRootId(idList.next());
            }
        });
        release(index);
    }

    public BPlusTree makeDirs(Path path){
        Path indexPath = Path.ROOT;
        BPlusTree index = rootIndex;
        for (String name: path.getNames()){
            FileBlock leaf = (FileBlock) index.find(name);
            if (leaf == null){
                leaf = new FileBlock();
                leaf.setName(name);
                leaf.setType(EnumFileType.DIRECTORY.getType());
                leaf.setCreateTime(System.currentTimeMillis());
                FileBlock finalLeaf = leaf;
                index.addIfAbsent(
                        leaf,
                        k -> ((FileBlock) k).setChildRootId(idList.next()),
                        (leafOld) -> finalLeaf.setChildRootId(((FileBlock) leafOld).getChildRootId()));
                if (leaf.getChildRootId() == -1){
                    leaf.setChildRootId(finalLeaf.getChildRootId());
                }
            }
            if (leaf.getType() != EnumFileType.DIRECTORY.getType()){
                throw new FileAlreadyExistsException(name);
            }
            indexPath = new Path(indexPath, name);
            long treeId = leaf.getChildRootId();
            BPlusTree newIndex = allocation(indexPath, treeId, idList, allocation);
            release(index);
            index = newIndex;
        }
        return index;
    }

    private BPlusTree findIndexWithPath(Path path){
        Path indexPath = Path.ROOT;
        BPlusTree index = rootIndex;
        for (String name: path.getNames()){
            FileBlock leaf = (FileBlock) index.find(name);
            if (leaf == null || leaf.getType() != EnumFileType.DIRECTORY.getType()){
                throw new FileNotFoundException(name);
            }
            indexPath = new Path(indexPath, name);
            long treeId = leaf.getChildRootId();
            BPlusTree newIndex = allocation(indexPath, treeId, idList, allocation);
            release(index);
            index = newIndex;
        }
        return index;
    }

    public Paged<FileBlock> list(Path path, String lastName, int paged){
        BPlusTree index = findIndexWithPath(path);
        AtomicInteger count = new AtomicInteger(0);
        List<FileBlock> list = new ArrayList<>();
        String newLastName = (String) index.foreach((leaf) -> {
            list.add((FileBlock) leaf);
            if (count.getAndIncrement() >= paged) {
                return leaf.getName();
            }
            return null;
        }, lastName);
        Paged<FileBlock> pagedList = new Paged<>(list, newLastName);
        release(index);
        return pagedList;
    }

    public Paged<FileBlock> list(Path path, String lastName){
        BPlusTree index = findIndexWithPath(path);
        List<FileBlock> list = new ArrayList<>();
        String newLastName = (String) index.foreach((leaf) -> {
            list.add((FileBlock) leaf);
            return null;
        }, lastName);
        Paged<FileBlock> paged = new Paged<>(list, newLastName);
        release(index);
        return paged;
    }

    public Paged<FileBlock> find(Path path, String prefix, String lastName, int paged){
        BPlusTree index = findIndexWithPath(path);
        AtomicInteger count = new AtomicInteger(0);
        List<FileBlock> list = new ArrayList<>();
        String newLastName = (String) index.findWithPrefix(prefix, (leaf) -> {
            list.add((FileBlock) leaf);
            if (count.getAndIncrement() >= paged) {
                return leaf.getName();
            }
            return null;
        }, lastName);
        Paged<FileBlock> pagedList = new Paged<>(list, newLastName);
        release(index);
        return pagedList;
    }

    public FileBlock fetch(Path path){
        BPlusTree index = findIndexWithPath(path.getParent());
        FileBlock fileBlock = (FileBlock) index.find(path.getName());
        release(index);
        return fileBlock;
    }

    public FileBlock edit(Path path, FileBlock newBlock){
        if (!newBlock.getName().equals(path.getName())){
            throw new CodeException();
        }
        BPlusTree index = findIndexWithPath(path.getParent());
        FileBlock edit = (FileBlock) index.edit(newBlock);
        release(index);
        return edit;
    }

    public void rename(Path path, FileBlock newBlock){
        BPlusTree index = findIndexWithPath(path.getParent());
        index.delete(path.getName());
        index.add(newBlock);
        release(index);
    }


    private final HashMap<Long, BPlusTree> map = new HashMap<>();

    private final HashMap<Long, AtomicInteger> referenceCount = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    public BPlusTree allocation(Path path, long id, LongList idList, DiskBlockAllocation<SerializeHeader, Serialize> allocation){
        lock.lock();
        try {
            BPlusTree tree = map.get(id);
            if (tree == null) {
                referenceCount.put(id, new AtomicInteger(1));
                tree = new BPlusTree(id, idList, allocation);
                map.put(id, tree);
                return tree;
            }else {
                referenceCount.get(id).incrementAndGet();
                return tree;
            }
        }finally {
            lock.unlock();
        }
    }

    public void release(BPlusTree index){
        lock.lock();
        try {
            AtomicInteger count = this.referenceCount.get(index.getId());
            if (count == null){
                return ;
            }
            int i = count.get();
            if (i <= 1) {
                this.map.remove(index.getId());
                this.referenceCount.remove(index.getId());
            }else {
                count.decrementAndGet();
            }
        }finally {
            lock.unlock();
        }
    }

}
