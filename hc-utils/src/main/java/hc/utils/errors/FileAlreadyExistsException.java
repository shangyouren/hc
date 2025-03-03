package hc.utils.errors;

public class FileAlreadyExistsException extends RuntimeException{

    public FileAlreadyExistsException(String name){
        super("File already exists " + name);
    }
}
