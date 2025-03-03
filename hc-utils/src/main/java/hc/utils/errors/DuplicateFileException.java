package hc.utils.errors;

public class DuplicateFileException extends RuntimeException
{
    public DuplicateFileException(String filename){
        super("File " + filename + " duplicate");
    }
}
