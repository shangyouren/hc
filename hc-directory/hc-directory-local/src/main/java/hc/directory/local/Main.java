package hc.directory.local;


import cn.hutool.core.collection.ListUtil;
import hc.directory.local.capability.DirectoryService;
import hc.directory.local.capability.DirectoryServiceImplWithIndex;
import hc.directory.local.constants.EnumFileType;
import hc.directory.local.mapping.MappedByteBufferMappingFactory;
import hc.directory.local.pojo.FileBlock;
import hc.directory.local.pojo.Paged;
import hc.directory.local.pojo.Path;
import hc.utils.convert.ProjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main
{

    public static final String DIRECTORY = "test-direct";


    public static void main(String[] args) {
        File baseDir = ProjectUtils.directoryPathWithCheck(DIRECTORY);
        DirectoryService directoryService = new DirectoryServiceImplWithIndex(
                baseDir, 4 * 1024,4 * 1024,
                40000, 20000,
                new MappedByteBufferMappingFactory(),
                false);
        testTerminal(directoryService);
//        testWrite(directoryService);
    }

    private static void testTerminal(DirectoryService directoryService){
        Scanner scanner = new Scanner(System.in);
        String cmd;
        Path path = new Path(Path.ROOT_NAME);
        while ((cmd = scanner.nextLine()) != null){
            String[] split = StringUtils.split(cmd);
            if (split.length <= 0){
                continue;
            }
            if (split[0].equals("cd")){
                if (split[1].equals("..")){
                    path = path.getParent();
                }else {
                    path = new Path(path, split[1]);
                }
                System.out.println("PWD:" + path);
            }else if (split[0].equals("ls")){
                long time = System.currentTimeMillis();
                Paged<FileBlock> list = directoryService.list(path, null);
                System.out.println("【lost：" + (System.currentTimeMillis() - time) + "】");
                int c= 0;
                for (FileBlock datum : list.getData()) {
                    System.out.print(datum.getName() + "    ");
                    c++;
                    if (c % 5 == 0){
                        System.out.println();
                    }
                }
                if (c % 5 != 0){
                    System.out.println();
                }
            }
        }
    }

    private static void testWrite(DirectoryService directoryService){
        File testDir = new File("D:\\");
        List<File> files = new ArrayList<>();
        scanFile(files, testDir);
        files.remove(0);
        files = new HashSet<>(files).stream().toList();
        List<List<File>> split = ListUtil.split(files, 10000);
        long time = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
        for (List<File> fileList : split) {
            new Thread(() -> {
                for (File file : fileList) {
                    Path path = toPath(file);
                    FileBlock block = new FileBlock();
                    block.setName(path.getName());
                    block.setType(file.isDirectory() ? EnumFileType.DIRECTORY.getType() : EnumFileType.FILE.getType());
                    directoryService.addWithMakeDirs(block, path);
                    int i = count.incrementAndGet();
                    if (i % 10000 == 0){
                        System.out.println(i + " " + (System.currentTimeMillis() - time));
                    }
                }
            }).start();
        }
    }

    private static Path toPath(File file){
        return new Path(file.toString().replace("D:", "").replace("\\", "/"));
    }

    private static void scanFile(List<File> values, File dir){
        File[] files = dir.listFiles();
        if (files == null){
            values.add(dir);
            return ;
        }
        for (File file : files) {
            scanFile(values, file);
        }
    }
}
