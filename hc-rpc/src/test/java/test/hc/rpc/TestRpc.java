package test.hc.rpc;

import hc.rpc.Bootstrap;
import hc.rpc.pojo.RpcPackage;
import hc.rpc.pojo.Target;
import hc.rpc.service.RpcClient;
import hc.rpc.service.RpcServer;
import hc.rpc.service.RpcServerFactory;
import hc.rpc.service.impl.NettyRpcClient;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TestRpc
{

    @Test
    public void test() throws InterruptedException
    {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.startServer(TestRpcServer::new);
        RpcClient rpcClient = bootstrap.startClient();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++)
        {
            Mono<RpcPackage> response = rpcClient.request("hello shangyouren", new Target("localhost", 9005));
            int finalI = i;
            response.subscribe(rpcPackage -> {
                System.out.println("response: [" + rpcPackage.getValue() + "]");
                if (finalI % 10000 == 0){
                    System.out.println(System.currentTimeMillis() - time);
                }
            });
        }
        Thread.sleep(10000);
        NettyRpcClient.printChannel();
    }

    public static class TestRpcServer implements RpcServer{

        @Override
        public Mono<RpcPackage> accept(RpcPackage rpcPackage)
        {
            System.out.println("accept: [" + rpcPackage.getValue() + "]");
            RpcPackage response = RpcPackage.response(rpcPackage);
            response.setValue("hello world");
            return Mono.just(response);
        }
    }

}
