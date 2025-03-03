package hc.directory.local.disk.capability;

import hc.directory.local.disk.collect.btree.BPlusTree;
import hc.directory.local.disk.pojo.FileBlock;
import hc.directory.local.disk.pojo.Paged;
import hc.directory.local.disk.pojo.Path;

public interface DirectoryService
{

    void add(FileBlock block, Path path);

    void addWithMakeDirs(FileBlock block, Path path);

    BPlusTree makeDirs(Path path);

    Paged<FileBlock> list(Path path, String lastName, int paged);

    Paged<FileBlock> list(Path path, String lastName);

    Paged<FileBlock> find(Path path, String prefix, String lastName, int paged);

    FileBlock fetch(Path path);

    FileBlock edit(Path path, FileBlock newBlock);

    void rename(Path path, FileBlock newBlock);

}
