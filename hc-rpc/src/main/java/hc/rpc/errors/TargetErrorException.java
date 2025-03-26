package hc.rpc.errors;

import hc.rpc.pojo.Target;

public class TargetErrorException extends RuntimeException
{

    public TargetErrorException(Target target){
        super(String.format("Cannot client to target server %s:%d", target.getHost(), target.getPort()));
    }
}
