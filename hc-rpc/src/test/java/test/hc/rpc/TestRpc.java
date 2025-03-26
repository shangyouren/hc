package test.hc.rpc;

import hc.rpc.Bootstrap;
import hc.rpc.RpcConfig;
import hc.rpc.pojo.RpcPackage;
import hc.rpc.pojo.Target;
import hc.rpc.service.RpcClient;
import hc.rpc.service.RpcServer;
import lombok.Data;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.function.Function;

public class TestRpc
{

    @Test
    public void test() throws InterruptedException
    {
        Bootstrap bootstrap = new Bootstrap(new RpcConfig());
        bootstrap.startServer(TestRpcServer::new);

        RpcClient rpcClient = bootstrap.startClient();


        RpcConfig config2 = new RpcConfig();
        config2.setPort(9006);
        Bootstrap bootstrap2 = new Bootstrap(config2);
        bootstrap2.startServer(() -> new Test2RpcServer(rpcClient));

        long time = System.currentTimeMillis();
        ArrayList<Target> targets = new ArrayList<>();
        targets.add(new Target("localhost", 9006));
        targets.add(new Target("localhost", 9005));
        Flux<RpcPackage> response = rpcClient.request("hello shangyouren", targets);
        response.subscribe(rpcPackage -> System.out.println("response: [" + rpcPackage.getValue() + "]" + (System.currentTimeMillis() - time)));
        Thread.sleep(300);
    }

    @Data
    public static class Test2RpcServer implements RpcServer{

        private final RpcClient client;

        @Override
        public Mono<RpcPackage> accept(RpcPackage rpcPackage)
        {
            System.out.println("accept2: [" + rpcPackage.getValue() + "]");
            Mono<RpcPackage> response = client.request("hello shangyouren2", new Target("localhost", 9005));

            return response.flatMap((Function<RpcPackage, Mono<RpcPackage>>) rpcPackage1 ->
            {
                System.out.println("response2: [" + rpcPackage1.getValue() + "]");
                RpcPackage response2 = RpcPackage.response(rpcPackage1);
                response2.setValue("789");
                return Mono.just(response2);
            });

        }
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
