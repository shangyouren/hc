package hc.utils.errors;

public class CodeException extends RuntimeException
{

    public CodeException(){
        super();
    }


    public CodeException(String message){
        super(message);
    }

    public CodeException(Exception e) {
        super(e);
    }
}
