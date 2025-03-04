package hc.directory.local.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Path
{

    public static final char SPLIT = '/';

    public static final String ROOT_NAME = "/";
    public static final Path ROOT = new Path(ROOT_NAME);

    private final List<String> names;

    private final String path;

    public Path(String path){
        this.names = parse(path);
        this.path = path;
    }

    public Path(Path path, String name){
        this.names = path.getNames();
        this.names.add(name);
        this.path = path.path.equals(ROOT_NAME) ? path.path + name : path.path + SPLIT + name;
    }

    public Path getParent(){
        int index = path.lastIndexOf(SPLIT);
        if (index <= 0){
            if (this.path.equals(ROOT_NAME)){
                return this;
            }
            return new Path(ROOT_NAME);
        }
        return new Path(path.substring(0, index));
    }

    public String getName(){
        return names.get(names.size() - 1);
    }


    public int size(){
        return names.size();
    }

    public static List<String> parse(String path){
        char[] chars = path.toCharArray();
        int i = 0;
        while (chars[i] != SPLIT){
            i++;
        }
        List<String> r = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        while (i < chars.length){
            if (chars[i] == SPLIT){
                if (sb.length() > 0){
                    r.add(sb.toString().trim());
                    sb.setLength(0);
                }
            }else {
                sb.append(chars[i]);
            }
            i++;
        }
        if (sb.length() > 0){
            r.add(sb.toString());
        }
        return r;
    }

    @Override
    public String toString(){
        return path;
    }

}
