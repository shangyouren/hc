package hc.rpc.service;

import hc.rpc.pojo.RpcPackage;
import hc.rpc.pojo.Target;
import reactor.core.publisher.Mono;

public interface RpcClient
{

    Mono<RpcPackage> request(Object data, Target target);

    Mono<RpcPackage> request(Object data, Target target, long timeout);

}
