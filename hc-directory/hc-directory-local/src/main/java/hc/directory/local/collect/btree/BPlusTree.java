package hc.directory.local.collect.btree;

import hc.directory.local.constants.EnumFileType;
import hc.directory.local.mapping.DiskBlock;
import hc.directory.local.mapping.DiskBlockAllocation;
import hc.directory.local.mapping.Serialize;
import hc.directory.local.mapping.SerializeHeader;
import hc.utils.errors.CodeException;
import hc.utils.errors.FileAlreadyExistsException;
import hc.utils.errors.FileNotFoundException;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class BPlusTree {

    private final LongList idList;

    private final DiskBlockAllocation<SerializeHeader, Serialize> allocation;

    private final ReentrantReadWriteLock lock;

    private final ReentrantReadWriteLock.ReadLock readLock;

    private final ReentrantReadWriteLock.WriteLock writeLock;


    private final long id;

    private long rootBlockId;

    public BPlusTree(
            long id,
            LongList idList,
            DiskBlockAllocation<SerializeHeader, Serialize> allocation) {
        this.idList = idList;
        this.id = id;
        this.allocation = allocation;
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.rootBlockId = this.idList.read(id);
    }

    public void add(TreeLeaf value){
        add(value, true);
    }

    public void addWithoutException(TreeLeaf value){
        add(value, false);
    }
    public void addIfAbsent(TreeLeaf value, Consumer<TreeLeaf> operate){
        addIfAbsent(value, operate, null);
    }

    public void addIfAbsent(TreeLeaf value, Consumer<TreeLeaf> operate, Consumer<TreeLeaf> exceptionWhenExists){
        writeLock.lock();
        try
        {
            if (value.len() > allocation.getDiskBlockSize() / 3)
            {
                throw new CodeException();
            }
            DiskBlock<SerializeHeader, Serialize> block;
            if (rootBlockId == -1){
                block = allocation.newDiskBlock();
                syncRootBlockId(block.id());
            }else {
                block = allocation.read(this.rootBlockId);
            }
            AtomicReference<String> updateIndex = new AtomicReference<>();
            TreeLine line = addIfAbsentDfs(block, value, updateIndex, operate, exceptionWhenExists);
            if (line != null)
            {
                TreeLine oldLine = new TreeLine();
                oldLine.setName(updateIndex.get());
                oldLine.setType(EnumFileType.NON_LEAF.getType());
                oldLine.setPoint(block.id());
                DiskBlock<SerializeHeader, Serialize> newRootBlock = allocation.newDiskBlock();
                newRootBlock.add(oldLine);
                newRootBlock.add(line);
                syncRootBlockId(newRootBlock.id());
            }
        }finally
        {
            writeLock.unlock();
        }
    }

    private void syncRootBlockId(long newId){
        this.rootBlockId = newId;
        idList.sync(id, this.rootBlockId);
    }


    private void add(TreeLeaf value, boolean exceptionWhenExists) {
        writeLock.lock();
        try
        {
            
            if (value.len() > allocation.getDiskBlockSize() / 3)
            {
                throw new CodeException();
            }
            DiskBlock<SerializeHeader, Serialize> block;
            if (rootBlockId == -1){
                block = allocation.newDiskBlock();
                syncRootBlockId(block.id());
            }else {
                block = allocation.read(this.rootBlockId);
            }
            AtomicReference<String> updateIndex = new AtomicReference<>();
            TreeLine line = addDfs(block, value, updateIndex, exceptionWhenExists);
            if (line != null)
            {
                TreeLine oldLine = new TreeLine();
                oldLine.setName(updateIndex.get());
                oldLine.setType(EnumFileType.NON_LEAF.getType());
                oldLine.setPoint(block.id());
                DiskBlock<SerializeHeader, Serialize> newRootBlock = allocation.newDiskBlock();
                newRootBlock.add(oldLine);
                newRootBlock.add(line);
                syncRootBlockId(newRootBlock.id());
            }
        }finally
        {
            writeLock.unlock();
        }
    }

    public TreeLeaf find(String name) {
        readLock.lock();
        try
        {
            
            long rootBlockId = this.rootBlockId;
            if (rootBlockId == -1){
                return null;
            }
            DiskBlock<SerializeHeader, Serialize> block = allocation.read(rootBlockId);
            return findDfs(block, name);
        }finally
        {
            readLock.unlock();
        }
    }

    public TreeLeaf edit(TreeLeaf value) {
        writeLock.lock();
        try
        {
            
            long rootBlockId = this.rootBlockId;
            if (rootBlockId == -1){
                return null;
            }
            DiskBlock<SerializeHeader, Serialize> block = allocation.read(rootBlockId);
            return editDfs(block, value);
        }finally
        {
            writeLock.unlock();
        }
    }

    public TreeLeaf editIfExcept(String name, Consumer<TreeLeaf> edit, Function<TreeLeaf, Boolean> except) {
        writeLock.lock();
        try
        {
            
            long rootBlockId = this.rootBlockId;
            if (rootBlockId == -1){
                return null;
            }
            DiskBlock<SerializeHeader, Serialize> block = allocation.read(rootBlockId);
            return editIfExceptDfs(block, name, edit, except);
        }finally
        {
            writeLock.unlock();
        }
    }

    public void printTree() {
        readLock.lock();
        try
        {
            
            long rootBlockId = this.rootBlockId;
            if (rootBlockId == -1){
                return ;
            }
            DiskBlock<SerializeHeader, Serialize> block = allocation.read(rootBlockId);
            printDfs(block, 1);
        }finally
        {
            readLock.unlock();
        }
    }

    public Object foreach(Function<TreeLeaf, Object> function, String lastName) {
        readLock.lock();
        try
        {
            long rootBlockId = this.rootBlockId;
            if (rootBlockId == -1){
                return null;
            }
            DiskBlock<SerializeHeader, Serialize> block = allocation.read(rootBlockId);
            return foreachDfs(block, 1, function, lastName);
        }finally
        {
            readLock.unlock();
        }
    }

    public TreeLeaf delete(String name) {
        writeLock.lock();
        try
        {
            
            long rootBlockId = this.rootBlockId;
            if (rootBlockId == -1){
                return null;
            }
            DiskBlock<SerializeHeader, Serialize> block = allocation.read(rootBlockId);
            AtomicReference<TreeLeaf> deleteObject = new AtomicReference<>();
            AtomicInteger rootChildSize = new AtomicInteger(0);
            deleteDfs(block, -1, name, null, null, null,
                    deleteObject, rootChildSize, new AtomicReference<>(null));
            if (rootChildSize.get() <= 1)
            {
                Object node = block.foreach((v, off) -> v);
                if (node instanceof TreeLine first)
                {
                    syncRootBlockId(first.getPoint());
                }
            }
            return deleteObject.get();
        }finally
        {
            writeLock.unlock();
        }
    }

    public Object findWithPrefix(String prefix, Function<TreeLeaf, Object> function, String lastName){
        readLock.lock();
        try
        {
            
            long rootBlockId = this.rootBlockId;
            if (rootBlockId == -1){
                return null;
            }
            DiskBlock<SerializeHeader, Serialize> block = allocation.read(rootBlockId);
            return findWithPrefixDfs(block, prefix, function, lastName);
        }finally
        {
            readLock.unlock();
        }
    }

    private Object findWithPrefixDfs(DiskBlock<SerializeHeader, Serialize> block, String prefix, Function<TreeLeaf, Object> function, String lastName) {
        Node firstHeader = (Node) block.headerForeach((header, offset) -> header);

        if (firstHeader == null || (firstHeader instanceof TreeLeaf.Header)) {
            // 没有索引，直接添加
            List<Serialize> leafs = block.values();
            leafs = leafs.stream()
                    .filter(t ->
                    {
                        String name = ((Node) t).getName();
                        return name.startsWith(prefix) && (lastName == null || name.compareTo(lastName) > 0);
                    })
                    .sorted(Comparator.comparing((t) -> ((Node) t).getName()))
                    .toList();
            for (Serialize leaf : leafs)
            {
                Object apply = function.apply((TreeLeaf) leaf);
                if (apply != null){
                    return apply;
                }
            }
            return null;
        }
        List<NodePoint> targetList = new ArrayList<>();
        NodePoint minPoint = new NodePoint();
        block.headerForeach((header, off) -> {
            TreeLine line = (TreeLine) header;
            String headerPrefix = header.getName().length() <= prefix.length() ? header.getName() : header.getName().substring(0, prefix.length());
            if (headerPrefix.compareTo(prefix) > 0){
                return null;
            }
            if (headerPrefix.compareTo(prefix) == 0){
                targetList.add(new NodePoint(line.getPoint(), line.getName(), off));
                return null;
            }
            if (header.getName().compareTo(prefix) < 0){
                if (minPoint.getName() == null || minPoint.getName().compareTo(header.getName()) < 0){
                    minPoint.setId(line.getPoint());
                    minPoint.setName(line.getName());
                    minPoint.setOffset(off);
                }
            }
            return null;
        });
        if (minPoint.getName() != null)
        {
            targetList.add(minPoint);
        }
        if (targetList.isEmpty()){
            return null;
        }
        targetList.sort(Comparator.comparing(NodePoint::getName));
        for (NodePoint point : targetList)
        {
            DiskBlock<SerializeHeader, Serialize> nextBlock = allocation.read(point.id);
            Object r = findWithPrefixDfs(nextBlock, prefix, function, lastName);
            if (r != null)
            {
                return r;
            }
        }
        // 从上边的循环必定能定位到一个block
        return null;
    }



    private TreeLeaf findDfs(DiskBlock<SerializeHeader, Serialize> block, String name) {
        Node firstHeader = (Node) block.headerForeach((header, offset) -> header);

        if (firstHeader == null || (firstHeader instanceof TreeLeaf.Header)) {
            // 没有索引，直接添加
            return (TreeLeaf) block.headerForeach((header, offset) -> {
                if (header.getName().equals(name)) {
                    return block.read(offset);
                }
                return null;
            });
        }
        AtomicReference<Long> nextBlockSize = new AtomicReference<>(null);
        AtomicReference<String> nextIndexName = new AtomicReference<>(null);
        block.headerForeach((header, off) -> {
            if (header.getName().compareTo(name) <= 0) {
                if (nextIndexName.get() == null || nextIndexName.get().compareTo(header.getName()) < 0) {
                    nextBlockSize.set(((TreeLine) header).getPoint());
                    nextIndexName.set(header.getName());
                }
            }
            return null;
        });
        if (nextBlockSize.get() == null) {
            return null;
        }
        // 从上边的循环必定能定位到一个block
        DiskBlock<SerializeHeader, Serialize> nextBlock = allocation.read(nextBlockSize.get());
        return findDfs(nextBlock, name);
    }

    private TreeLeaf editIfExceptDfs(DiskBlock<SerializeHeader, Serialize> block, String name, Consumer<TreeLeaf> edit, Function<TreeLeaf, Boolean> except) {
        Node firstHeader = (Node) block.headerForeach((header, offset) -> header);
        if (firstHeader == null || (firstHeader instanceof TreeLeaf.Header)) {
            // 没有索引，直接添加
            return (TreeLeaf) block.headerForeach((header, offset) -> {
                if (header.getName().equals(name)) {
                    TreeLeaf read = (TreeLeaf) block.read(offset);
                    int oldLen = read.len();
                    if (except.apply(read)){
                        edit.accept(read);
                        if (read.len() != oldLen){
                            throw new CodeException();
                        }
                        block.edit(read, offset);
                    }
                    return read;
                }
                return null;
            });
        }
        AtomicReference<Long> nextBlockSize = new AtomicReference<>(null);
        AtomicReference<String> nextIndexName = new AtomicReference<>(null);
        block.headerForeach((header, off) -> {
            if (header.getName().compareTo(name) <= 0) {
                if (nextIndexName.get() == null || nextIndexName.get().compareTo(header.getName()) < 0) {
                    nextBlockSize.set(((TreeLine) header).getPoint());
                    nextIndexName.set(header.getName());
                }
            }
            return null;
        });
        if (nextBlockSize.get() == null) {
            return null;
        }
        // 从上边的循环必定能定位到一个block
        DiskBlock<SerializeHeader, Serialize> nextBlock = allocation.read(nextBlockSize.get());
        return editIfExceptDfs(nextBlock, name, edit, except);
    }

    private TreeLeaf editDfs(DiskBlock<SerializeHeader, Serialize> block, TreeLeaf value) {
        Node firstHeader = (Node) block.headerForeach((header, offset) -> header);
        if (firstHeader == null || (firstHeader instanceof TreeLeaf.Header)) {
            // 没有索引，直接添加
            TreeLeaf oldValue = (TreeLeaf) block.headerForeach((header, offset) -> {
                if (header.getName().equals(value.getName())) {
                    Serialize old = block.read(offset);
                    if (old.len() != value.len()){
                        throw new CodeException("New value length != old value length");
                    }
                    block.edit(value, offset);
                    return old;
                }
                return null;
            });
            if (oldValue == null){
                throw new FileNotFoundException(value.getName());
            }

            return oldValue;
        }
        AtomicReference<Long> nextBlockSize = new AtomicReference<>(null);
        AtomicReference<String> nextIndexName = new AtomicReference<>(null);
        block.headerForeach((header, off) -> {
            if (header.getName().compareTo(value.getName()) <= 0) {
                if (nextIndexName.get() == null || nextIndexName.get().compareTo(header.getName()) < 0) {
                    nextBlockSize.set(((TreeLine) header).getPoint());
                    nextIndexName.set(header.getName());
                }
            }
            return null;
        });
        if (nextBlockSize.get() == null) {
            return null;
        }
        // 从上边的循环必定能定位到一个block
        DiskBlock<SerializeHeader, Serialize> nextBlock = allocation.read(nextBlockSize.get());
        return editDfs(nextBlock, value);
    }

    private TreeLine addDfs(DiskBlock<SerializeHeader, Serialize> block, TreeLeaf value, AtomicReference<String> updateIndex, boolean exceptionWhenExists) {
        Node firstHeader = (Node) block.headerForeach((header, offset) -> header);

        if (firstHeader == null || (firstHeader instanceof TreeLeaf.Header)) {
            // 没有索引，直接添加
            AtomicReference<String> min = new AtomicReference<>();
            Object result = block.headerForeach((header, offset) -> {
                if (min.get() == null || header.getName().compareTo(min.get()) < 0){
                    min.set(header.getName());
                }
                if (header.getName().equals(value.getName())) {
                    return offset;
                }
                return null;
            });
            if (result != null) {
                if (exceptionWhenExists) {
                    throw new FileAlreadyExistsException(value.getName());
                }else {
                    return null;
                }
            }
            boolean add = block.add(value);
            if (!add) {
                DiskBlock<SerializeHeader, Serialize> newBlock = allocation.newDiskBlock();
                return splitBlock(block, newBlock, value, updateIndex);
            }else {
                if (min.get() != null && min.get().compareTo(value.getName()) > 0){
                    updateIndex.set(value.getName());
                }
            }
            return null;
        }
        NodePoint nextBlockPoint = new NodePoint();
        NodePoint minBlockPoint = new NodePoint();
        block.headerForeach((header, off) -> {
            if (minBlockPoint.getName() == null || header.getName().compareTo(minBlockPoint.getName()) < 0){
                minBlockPoint.setId(((TreeLine) header).getPoint());
                minBlockPoint.setName(header.getName());
                minBlockPoint.setOffset(off);
            }
            if (header.getName().compareTo(value.getName()) <= 0) {
                if (nextBlockPoint.getName() == null || nextBlockPoint.getName().compareTo(header.getName()) < 0) {
                    nextBlockPoint.setId(((TreeLine) header).getPoint());
                    nextBlockPoint.setOffset(off);
                    nextBlockPoint.setName(header.getName());
                }
            }
            return null;
        });
        if (nextBlockPoint.getName() == null){
            nextBlockPoint.setName(minBlockPoint.getName());
            nextBlockPoint.setOffset(minBlockPoint.getOffset());
            nextBlockPoint.setId(minBlockPoint.getId());
        }
        // 从上边的循环必定能定位到一个block
        DiskBlock<SerializeHeader, Serialize> nextBlock = allocation.read(nextBlockPoint.getId());
        AtomicReference<String> updateIndexWithoutSplitBlock = new AtomicReference<>();
        TreeLine newLine = addDfs(nextBlock, value, updateIndexWithoutSplitBlock, exceptionWhenExists);
        if (updateIndexWithoutSplitBlock.get() != null && !updateIndexWithoutSplitBlock.get().equals(nextBlockPoint.getName())){
            TreeLine line = new TreeLine();
            line.setType(EnumFileType.NON_LEAF.getType());
            line.setPoint(nextBlock.id());
            line.setName(updateIndexWithoutSplitBlock.get());
            block.edit(line, nextBlockPoint.getOffset());
        }
        if (minBlockPoint.getName().compareTo(value.getName()) > 0){
            updateIndex.set(value.getName());
        }
        if (newLine != null) {
            // 下层新加了一个
            boolean add = block.add(newLine);
            if (!add) {
                //当前block空间不够，需要分裂
                DiskBlock<SerializeHeader, Serialize> newBlock = allocation.newDiskBlock();
                return splitBlock(block, newBlock, newLine, updateIndex);
            }
        }
        return null;
    }

    private TreeLine addIfAbsentDfs(
            DiskBlock<SerializeHeader, Serialize> block,
            TreeLeaf value,
            AtomicReference<String> updateIndex,
            Consumer<TreeLeaf> operate,
            Consumer<TreeLeaf> exceptionWhenExists
    ) {
        Node firstHeader = (Node) block.headerForeach((header, offset) -> header);

        if (firstHeader == null || (firstHeader instanceof TreeLeaf.Header)) {
            // 没有索引，直接添加
            AtomicReference<String> min = new AtomicReference<>();
            Object result = block.headerForeach((header, offset) -> {
                if (min.get() == null || header.getName().compareTo(min.get()) < 0){
                    min.set(header.getName());
                }
                if (header.getName().equals(value.getName())) {
                    return block.read(offset);
                }
                return null;
            });
            if (result != null) {
                if (exceptionWhenExists == null) {
                    throw new FileAlreadyExistsException(value.getName());
                }else {
                    exceptionWhenExists.accept((TreeLeaf) result);
                    return null;
                }
            }
            operate.accept(value);
            boolean add = block.add(value);
            if (!add) {
                DiskBlock<SerializeHeader, Serialize> newBlock = allocation.newDiskBlock();
                return splitBlock(block, newBlock, value, updateIndex);
            }else {
                if (min.get() != null && min.get().compareTo(value.getName()) > 0){
                    updateIndex.set(value.getName());
                }
            }
            return null;
        }
        NodePoint nextBlockPoint = new NodePoint();
        NodePoint minBlockPoint = new NodePoint();
        block.headerForeach((header, off) -> {
            if (minBlockPoint.getName() == null || header.getName().compareTo(minBlockPoint.getName()) < 0){
                minBlockPoint.setId(((TreeLine) header).getPoint());
                minBlockPoint.setName(header.getName());
                minBlockPoint.setOffset(off);
            }
            if (header.getName().compareTo(value.getName()) <= 0) {
                if (nextBlockPoint.getName() == null || nextBlockPoint.getName().compareTo(header.getName()) < 0) {
                    nextBlockPoint.setId(((TreeLine) header).getPoint());
                    nextBlockPoint.setOffset(off);
                    nextBlockPoint.setName(header.getName());
                }
            }
            return null;
        });
        if (nextBlockPoint.getName() == null){
            nextBlockPoint.setName(minBlockPoint.getName());
            nextBlockPoint.setOffset(minBlockPoint.getOffset());
            nextBlockPoint.setId(minBlockPoint.getId());
        }
        // 从上边的循环必定能定位到一个block
        DiskBlock<SerializeHeader, Serialize> nextBlock = allocation.read(nextBlockPoint.getId());
        AtomicReference<String> updateIndexWithoutSplitBlock = new AtomicReference<>();
        TreeLine newLine = addIfAbsentDfs(nextBlock, value, updateIndexWithoutSplitBlock, operate, exceptionWhenExists);
        if (updateIndexWithoutSplitBlock.get() != null && !updateIndexWithoutSplitBlock.get().equals(nextBlockPoint.getName())){
            TreeLine line = new TreeLine();
            line.setType(EnumFileType.NON_LEAF.getType());
            line.setPoint(nextBlock.id());
            line.setName(updateIndexWithoutSplitBlock.get());
            block.edit(line, nextBlockPoint.getOffset());
        }
        if (minBlockPoint.getName().compareTo(value.getName()) > 0){
            updateIndex.set(value.getName());
        }
        if (newLine != null) {
            // 下层新加了一个
            boolean add = block.add(newLine);
            if (!add) {
                //当前block空间不够，需要分裂
                DiskBlock<SerializeHeader, Serialize> newBlock = allocation.newDiskBlock();
                return splitBlock(block, newBlock, newLine, updateIndex);
            }
        }
        return null;
    }

    private TreeLine splitBlock(
            DiskBlock<SerializeHeader, Serialize> old,
            DiskBlock<SerializeHeader, Serialize> newBlock,
            Serialize data, AtomicReference<String> updateIndex) {
        List<Serialize> values = old.values();
        String oldIndex = null;
        for (Serialize value : values) {
            if (oldIndex == null || ((Node) value).getName().compareTo(oldIndex) < 0){
                oldIndex = ((Node) value).getName();
            }
        }
        values.add(data);
        values.sort(Comparator.comparing(t -> ((Node) t).getName()));
        int len = values.size();
        List<Serialize> oldList = new ArrayList<>(len / 2 + 1);
        List<Serialize> newList = new ArrayList<>(len / 2 + 1);
        for (int i = 0; i < len; i++) {
            if (i <= len / 2) {
                oldList.add(values.get(i));
            } else {
                newList.add(values.get(i));
            }
        }
        old.clearAndSet(oldList);
        newBlock.clearAndSet(newList);
        TreeLine newLine = new TreeLine();
        newLine.setName(((Node) newList.get(0)).getName());
        newLine.setType(EnumFileType.NON_LEAF.getType());
        newLine.setPoint(newBlock.id());
        String oldListNewIndex = ((Node) oldList.get(0)).getName();
        updateIndex.set(oldListNewIndex);
        return newLine;
    }



    private void printDfs(DiskBlock<SerializeHeader, Serialize> block, int deep) {
        StringBuilder str = new StringBuilder("DEEP:" + deep + ";" + block.id());
        List<Long> childBlockIds = new ArrayList<>();
        block.headerForeach((data, offset) -> {
            if (data instanceof TreeLeaf.Header) {
                str.append("{").append("0:").append(data.getName()).append("}");
            } else if (data instanceof TreeLine) {
                long point = ((TreeLine) data).getPoint();
                childBlockIds.add(point);
                str.append("{").append("1:").append(data.getName()).append(":").append(point).append("}");
            } else {
                throw new RuntimeException();
            }
            return null;
        });
        str.append("SIZE: ").append(childBlockIds.size());
        System.out.println(str);
        for (Long childBlockId : childBlockIds) {
            DiskBlock<SerializeHeader, Serialize> read = allocation.read(childBlockId);
            printDfs(read, deep + 1);
        }
    }

    private Object foreachDfs(DiskBlock<SerializeHeader, Serialize> block, int deep, Function<TreeLeaf, Object> function, String lastName) {
        List<Object> values = new ArrayList<>();
        List<String> names = new ArrayList<>();
        NodePoint minPoint = new NodePoint();
        block.headerForeach((data, offset) ->
        {
            if (data instanceof TreeLeaf.Header)
            {
                if (lastName != null && data.getName().compareTo(lastName) <= 0){
                    return null;
                }
                names.add(data.getName());
                values.add(block.read(offset));
            }
            else if (data instanceof TreeLine)
            {
                if (lastName != null && data.getName().compareTo(lastName) <= 0){
                    if (minPoint.getName() == null || minPoint.getName().compareTo(data.getName()) < 0){
                        minPoint.setName(data.getName());
                        minPoint.setId(((TreeLine) data).getPoint());
                        minPoint.setOffset(offset);
                    }
                    return null;
                }
                long point = ((TreeLine) data).getPoint();
                values.add(point);
                names.add(data.getName());
            }
            else
            {
                throw new RuntimeException();
            }
            return null;
        });
        if (minPoint.getName() != null){
            values.add(minPoint.getId());
            names.add(minPoint.getName());
        }
        if (values.size() <= 0){
            return null;
        }
        // sort
        String[] namesArray = names.toArray(new String[names.size()]);
        Object[] valuesArray = values.toArray();
        sort(namesArray, valuesArray);
        if (valuesArray[0] instanceof Long)
        {
            for (Object childBlockId : valuesArray)
            {
                DiskBlock<SerializeHeader, Serialize> read = allocation.read((Long) childBlockId);
                Object r = foreachDfs(read, deep + 1, function, lastName);
                if (r != null)
                {
                    return r;
                }
            }
        }else {
            for (Object value : valuesArray)
            {
                Object apply = function.apply((TreeLeaf) value);
                if (apply != null){
                    return apply;
                }
            }
        }
        return null;
    }

    private static void sort(String[] names, Object[] values){
        var len = names.length;
        for (var i = 0; i < len - 1; i++) {
            for (var j = 0; j < len - 1 - i; j++) {
                if (names[j].compareTo(names[j + 1]) > 0) {        // 相邻元素两两对比
                    String temp = names[j + 1];        // 元素交换
                    Object temp2 = values[j + 1];
                    names[j+1] = names[j];
                    values[j+1] = values[j];
                    names[j] = temp;
                    values[j] = temp2;
                }
            }
        }
    }

    private Integer deleteDfs(
            DiskBlock<SerializeHeader, Serialize> block, Integer blockInParentOffset,
            String name, Long lastBlockId, Long nextBlockId, Integer nextBlockInParentOffset,
            AtomicReference<TreeLeaf> deleteObject, AtomicInteger blockChildSize, AtomicReference<String> needUpdateBlockIndex
    ) {
        Node firstHeader = (Node) block.headerForeach((header, offset) -> header);
        if (firstHeader == null || (firstHeader instanceof TreeLeaf.Header)) {
            // 叶子节点，直接删除
            AtomicReference<String> min = new AtomicReference<>();
            AtomicReference<String> oriMin = new AtomicReference<>();
            AtomicReference<Integer> deleteOffset = new AtomicReference<>();
            block.headerForeach((header, offset) -> {
                if (blockChildSize != null){
                    blockChildSize.getAndIncrement();
                }
                if (oriMin.get() == null || oriMin.get().compareTo(header.getName()) >= 0){
                    oriMin.set(header.getName());
                }
                if (header.getName().equals(name)) {
                    deleteOffset.set(offset);
                    return null;
                }else {
                    if (min.get() == null || header.getName().compareTo(min.get()) <= 0){
                        min.set(header.getName());
                    }
                }
                return null;
            });
            if (deleteOffset.get() != null){
                Serialize read = block.read(deleteOffset.get());
                block.delete(deleteOffset.get());
                deleteObject.set((TreeLeaf)read);
            }
            Integer releaseNodeInParentOffset = checkAndRelease(block, blockInParentOffset, lastBlockId, nextBlockId, nextBlockInParentOffset);
            if (releaseNodeInParentOffset == null && oriMin.get().equals(name)){
                // 未能合并叶子节点block，须向上一层汇报修改block最新header
                needUpdateBlockIndex.set(min.get());
            }
            return releaseNodeInParentOffset;
        }
        NodePoint nextLevelBlockPoint = new NodePoint();
        NodePoint nextLevelBlockLastPoint = new NodePoint();
        NodePoint nextLevelBlockNextPoint = new NodePoint();
        block.headerForeach((header, off) -> {
            if (blockChildSize != null) {
                blockChildSize.incrementAndGet();
            }
            // 根据一次循环找到nextBlock的上下文
            if (header.getName().compareTo(name) <= 0) {
                if (nextLevelBlockPoint.getName() == null || nextLevelBlockPoint.getName().compareTo(header.getName()) < 0){
                    if (nextLevelBlockPoint.getName() != null){
                        if (nextLevelBlockLastPoint.getName() == null || nextLevelBlockLastPoint.getName().compareTo(nextLevelBlockPoint.getName()) < 0){
                            nextLevelBlockLastPoint.setName(nextLevelBlockPoint.getName());
                            nextLevelBlockLastPoint.setOffset(nextLevelBlockPoint.getOffset());
                            nextLevelBlockLastPoint.setId(nextLevelBlockPoint.getId());
                        }
                    }
                    nextLevelBlockPoint.setId(((TreeLine) header).getPoint());
                    nextLevelBlockPoint.setOffset(off);
                    nextLevelBlockPoint.setName(header.getName());
                }else if (nextLevelBlockLastPoint.getName() == null || nextLevelBlockLastPoint.getName().compareTo(header.getName()) < 0){
                    nextLevelBlockLastPoint.setName(header.getName());
                    nextLevelBlockLastPoint.setOffset(off);
                    nextLevelBlockLastPoint.setId(((TreeLine) header).getPoint());
                }
            }else {
                if (nextLevelBlockNextPoint.getName() == null || nextLevelBlockNextPoint.getName().compareTo(header.getName()) > 0){
                    nextLevelBlockNextPoint.setName(header.getName());
                    nextLevelBlockNextPoint.setOffset(off);
                    nextLevelBlockNextPoint.setId(((TreeLine) header).getPoint());
                }
            }
            return null;
        });
        if (nextLevelBlockPoint.getName() == null) {
            // 代表该值不再当前block中，比当前bloc的任何一个value都小，其实是代码错误
            return null;
        }
        // 从上边的循环必定能定位到一个block
        DiskBlock<SerializeHeader, Serialize> nextLevelBlock = allocation.read(nextLevelBlockPoint.getId());
        AtomicReference<String> nextLevelUpdateBlockIndex = new AtomicReference<>();
        Integer releaseBlockOffset = deleteDfs(
                nextLevelBlock, nextLevelBlockPoint.getOffset(),
                name,
                nextLevelBlockLastPoint.getId(),
                nextLevelBlockNextPoint.getId(), nextLevelBlockNextPoint.getOffset(),
                deleteObject, null, nextLevelUpdateBlockIndex
        );
        if (releaseBlockOffset != null){
            if (blockChildSize != null) {
                blockChildSize.decrementAndGet();
            }
            block.delete(releaseBlockOffset);
            return checkAndRelease(block, blockInParentOffset, lastBlockId, nextBlockId, nextBlockInParentOffset);
        }
        if (nextLevelUpdateBlockIndex.get() != null){
            // next block find index
            TreeLine line = new TreeLine();
            line.setType(EnumFileType.NON_LEAF.getType());
            line.setName(nextLevelUpdateBlockIndex.get());
            line.setPoint(nextLevelBlock.id());
            block.edit(line, nextLevelBlockPoint.getOffset());
        }
        return null;
    }

    private Integer checkAndRelease(
            DiskBlock<SerializeHeader, Serialize> block, Integer blockInParentOffset,
            Long lastBlockId,
            Long nextBlockId, Integer nextBlockInParentOffset
    ){
        if (block.getValueLen() == 0){
            allocation.release(block.id());
            return blockInParentOffset;
        }
        if (block.getValueLen() + block.metadataLen() < block.getSize() / 3){
            if (lastBlockId != null) {
                DiskBlock<SerializeHeader, Serialize> lastBlock = allocation.read(lastBlockId);
                if (lastBlock.getValueLen() + lastBlock.metadataLen() < lastBlock.getSize() / 2) {
                    return releaseBlock(lastBlock, block, blockInParentOffset);
                }
            }
            if (nextBlockId != null) {
                DiskBlock<SerializeHeader, Serialize> nextBlock = allocation.read(nextBlockId);
                if (nextBlock.getValueLen() + nextBlock.metadataLen() < nextBlock.getSize() / 2) {
                    return releaseBlock(block, nextBlock, nextBlockInParentOffset);
                }
            }
        }
        return null;
    }

    private Integer releaseBlock(
            DiskBlock<SerializeHeader, Serialize> block,
            DiskBlock<SerializeHeader, Serialize> next,
            int nextBlockInParentOffset
    ){
        List<Serialize> values = next.values();
        block.addAll(values);
        allocation.release(next.id());
        return nextBlockInParentOffset;
    }

    @Data
    private static class NodePoint{

        private Long id;

        private String name;

        private Integer offset;

        public NodePoint(long point, String name, Integer off)
        {
            this.id = point;
            this.name = name;
            this.offset = off;
        }

        public NodePoint(){}
    }

}
