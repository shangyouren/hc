package hc.utils.errors;

import java.io.File;

public class FileLengthException extends RuntimeException
{

    public FileLengthException(File file){
        super("File " + file + " Contaminated");
    }

    public FileLengthException(long len){
        super("Value len " + len + " Contaminated");
    }
}
