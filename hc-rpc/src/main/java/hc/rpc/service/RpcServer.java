package hc.rpc.service;

import hc.rpc.pojo.RpcPackage;
import reactor.core.publisher.Mono;

public interface RpcServer
{

    Mono<RpcPackage> accept(RpcPackage rpcPackage);
}
