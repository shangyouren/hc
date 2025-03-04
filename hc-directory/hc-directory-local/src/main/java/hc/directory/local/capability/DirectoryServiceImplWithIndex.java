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
import hc.utils.errors.FileNotFoundException;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class DirectoryServiceImplWithIndex implements DirectoryService
{

    private final DiskBlockAllocation<SerializeHeader, Serialize> indexAllocation;

    private final DiskBlockAllocation<SerializeHeader, Serialize> directoryAllocation;

    private final BPlusTree directoryList;

    private final LongList indexIdList;

    private final LongList directoryIdList;

    public DirectoryServiceImplWithIndex(
            File baseDir,
            int blockSize,
            int directoryBlockSize,
            int blockNumSize,
            int cacheSize,
            DiskMappingFactory mappingFactory,
            boolean releaseQueueSyncInstantly
    ){
        File indexBaseDir = new File(baseDir, "INDEX");
        this.indexAllocation = new DiskBlockAllocation<>(
                indexBaseDir, blockSize,
                new TreeLineDeserialize(new FileBlockLeafDeserialize()),
                blockNumSize, cacheSize,
                mappingFactory, releaseQueueSyncInstantly
        );
        File directoryBaseDir = new File(baseDir, "directory");
        this.directoryAllocation = new DiskBlockAllocation<>(
                directoryBaseDir, directoryBlockSize,
                new TreeLineDeserialize(new DirectoryIndexTreeLeafDeserialize()),
                blockNumSize, cacheSize,
                mappingFactory, releaseQueueSyncInstantly
        );
        indexIdList = new LongList(new File(indexBaseDir, "ID.LIST"), releaseQueueSyncInstantly);
        directoryIdList = new LongList(new File(directoryBaseDir, "ID.LIST"), releaseQueueSyncInstantly);
        if (directoryIdList.fileSize() <= 0){
            long next = directoryIdList.next();
            if (next != 0){
                throw new CodeException();
            }
        }
        this.directoryList = new BPlusTree(0, directoryIdList, directoryAllocation);
        if (indexIdList.fileSize() <= 0){
            long next = indexIdList.next();
            if (next != 0){
                throw new CodeException();
            }
            DirectoryIndexTreeLeaf leaf = new DirectoryIndexTreeLeaf();
            leaf.setId(next);
            leaf.setName(Path.ROOT.toString());
            this.directoryList.add(leaf);
        }
    }

    public void add(FileBlock block, Path path){
        block.setCreateTime(System.currentTimeMillis());
        BPlusTree index = findIndexWithPath(path.getParent());
        index.addIfAbsent(block, k -> {
            if (((FileBlock) k).getType() == EnumFileType.DIRECTORY.getType()) {
                long next = indexIdList.next();
                ((FileBlock) k).setChildRootId(next);
                DirectoryIndexTreeLeaf value = new DirectoryIndexTreeLeaf();
                value.setName(path.toString());
                value.setId(next);
                directoryList.add(value);
            }
        });
        release(index);
    }

    public void addWithMakeDirs(FileBlock block, Path path){
        block.setCreateTime(System.currentTimeMillis());
        BPlusTree index = makeDirs(path.getParent());
        index.addIfAbsent(block, k -> {
            if (((FileBlock) k).getType() == EnumFileType.DIRECTORY.getType()) {
                long next = indexIdList.next();
                ((FileBlock) k).setChildRootId(next);
                DirectoryIndexTreeLeaf value = new DirectoryIndexTreeLeaf();
                value.setName(path.toString());
                value.setId(next);
                directoryList.add(value);
            }
        });
        release(index);
    }

    public BPlusTree makeDirs(Path path){
        DirectoryIndexTreeLeaf leaf = (DirectoryIndexTreeLeaf) this.directoryList.find(path.toString());
        if (leaf == null){
            BPlusTree parentTree = makeDirs(path.getParent());
            FileBlock value = new FileBlock();
            value.setType(EnumFileType.DIRECTORY.getType());
            value.setName(path.getName());
            AtomicReference<Long> newId = new AtomicReference<>();
            parentTree.addIfAbsent(value, (v) -> {
                long next = indexIdList.next();
                ((FileBlock) v).setChildRootId(next);
                newId.set(next);
                DirectoryIndexTreeLeaf directoryLeaf = new DirectoryIndexTreeLeaf();
                directoryLeaf.setName(path.toString());
                directoryLeaf.setId(next);
                directoryList.add(directoryLeaf);
            } , (v) -> newId.set(((FileBlock) v).getChildRootId()));
            BPlusTree allocation = allocation(path, newId.get(), indexIdList, indexAllocation);
            release(parentTree);
            return allocation;
        }else {
            return allocation(path, leaf.getId(), indexIdList, indexAllocation);
        }
    }

    private BPlusTree findIndexWithPath(Path path){
        DirectoryIndexTreeLeaf leaf = (DirectoryIndexTreeLeaf) this.directoryList.find(path.toString());
        if (leaf == null){
            throw new FileNotFoundException(path.toString());
        }
        return allocation(path, leaf.getId(), indexIdList, indexAllocation);
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


    private final ConcurrentHashMap<Long, BPlusTree> map = new ConcurrentHashMap<>();

    public BPlusTree allocation(Path path, long id, LongList idList, DiskBlockAllocation<SerializeHeader, Serialize> allocation){
        return map.computeIfAbsent(id, i -> new BPlusTree(id, idList, allocation));
    }

    public void release(BPlusTree index){
    }

}
