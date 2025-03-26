package hc.rpc;

import lombok.Data;

import java.util.Random;

@Data
public class RpcConfig
{

    private long defaultTaskTimeout = 5000;

    private int bossGroupThreads = 1;

    private int workGroupThreads = 4;

    private int sendBufSize = 8 * 1024;

    private int receiverBufSize = 8 * 1024;

    private int port = 9005;

    private int workId = new Random().nextInt(1, 32 * 32);

}
