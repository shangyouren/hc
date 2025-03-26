package hc.rpc.service;

import hc.rpc.pojo.RpcPackage;
import hc.rpc.pojo.Target;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RpcClient
{

    Mono<RpcPackage> request(Object data, Target target);

    Mono<RpcPackage> request(Object data, Target target, long timeout);

    Flux<RpcPackage> request(Object data, List<Target> targets);

    Flux<RpcPackage> request(Object data, List<Target> targets, long timeout);
}
