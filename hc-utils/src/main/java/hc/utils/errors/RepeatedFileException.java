package hc.utils.errors;

public class RepeatedFileException extends RuntimeException
{

    public RepeatedFileException(String filename){
        super("File " + filename + " repeated");
    }

}
