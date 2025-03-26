package hc.rpc.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Target
{

    private String host;

    private int port;

    @Override
    public String toString(){
        return String.format("%s:%d", host, port);
    }

}
