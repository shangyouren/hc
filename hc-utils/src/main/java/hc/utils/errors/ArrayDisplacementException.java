package hc.utils.errors;

public class ArrayDisplacementException extends RuntimeException
{

    public ArrayDisplacementException(String cls, String type, String message){
        super(cls + "-" + type + ": " + message);
    }

}
