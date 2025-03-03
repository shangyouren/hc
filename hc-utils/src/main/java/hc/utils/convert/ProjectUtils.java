package hc.utils.convert;



import hc.utils.constants.PropertiesConstants;
import hc.utils.errors.IOException;

import java.io.File;
import java.net.URL;

public class ProjectUtils
{

    public static final String _STORAGE_DATA_FORMAT = "%s/data/%s";

    public static final String _CONFIG_FORMAT = "%s/config/%s";

    public static final String _CONFIG_BASE_FORMAT = "%s/%s";

    public static File filePath(String path)
    {
        return new File(String.format(
                _STORAGE_DATA_FORMAT,
                System.getProperty(PropertiesConstants._USER_DIR),
                path
        ));
    }

    public static File filePathWithCheck(String path){
        File file = filePath(path);
        checkFile(file);
        return file;
    }

    public static File directoryPathWithCheck(String path){
        File file = filePath(path);
        checkDirectory(file);
        return file;
    }

//    public static File directoryPathWithCheck(String path){
//        File file = filePath(path);
//        if (!file.isDirectory()){
//            boolean mkdirs = file.getParentFile().mkdirs();
//            if (!mkdirs){
//                throw new IOException(file, "create");
//            }
//        }
//        return file;
//    }

    public static File configPath(String fileName)
    {
        File file = new File(String.format(_CONFIG_FORMAT, System.getProperty(PropertiesConstants._USER_DIR), fileName));
        if (file.isFile())
        {
            return file;
        }
        file = new File(String.format(_CONFIG_FORMAT, System.getProperty(PropertiesConstants._USER_DIR), fileName));
        if (file.isFile())
        {
            return file;
        }
        URL resource = ProjectUtils.class.getResource("/" + fileName);
        if (resource != null)
        {
            String filePath = resource.getFile();
            file = new File(filePath);
            if (file.isFile())
            {
                return file;
            }
        }
        return null;
    }

    public static void checkFile(File file)
    {
        if (!file.getParentFile().isDirectory()){
            boolean mkdirs = file.getParentFile().mkdirs();
            if (!mkdirs){
                throw new IOException(file, "create");
            }
        }
    }

    public static void checkDirectory(File file)
    {
        if (!file.isDirectory()){
            boolean mkdirs = file.mkdirs();
            if (!mkdirs){
                throw new IOException(file, "create");
            }
        }
    }
}
