package hc.utils.errors;

import java.io.File;

public class IOException extends RuntimeException
{

    public IOException(Exception e){
        super(e);
    }

    public IOException(File file, String operate)
    {
        super("Operate " + operate + " for file " + file + " failed");
    }
}
