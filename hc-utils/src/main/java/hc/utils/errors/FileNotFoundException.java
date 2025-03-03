package hc.utils.errors;

public class FileNotFoundException extends RuntimeException
{
    public FileNotFoundException(String filename){
        super("File " + filename + " not found");
    }
}
