package hc.rpc.service.impl;

import cn.hutool.core.lang.Snowflake;
import hc.rpc.RpcConfig;
import hc.rpc.errors.ChannelUnsupprotException;
import hc.rpc.errors.TargetErrorException;
import hc.rpc.pojo.EnumPackageCode;
import hc.rpc.pojo.EnumPackageType;
import hc.rpc.pojo.RpcPackage;
import hc.rpc.pojo.Target;
import hc.rpc.service.RpcClient;
import hc.rpc.service.TaskManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NettyRpcClient implements RpcClient
{

    public NettyRpcClient(Bootstrap bootstrap, RpcConfig config){
        this.bootstrap = bootstrap;
        this.config = config;
        snowflake = new Snowflake(config.getWorkId() / 32, config.getWorkId() % 32);
    }

    private final Bootstrap bootstrap;

    private final RpcConfig config;

    private final Snowflake snowflake;

    private final static ConcurrentHashMap<String, ChannelPool> CHANNEL_MAP = new ConcurrentHashMap<>();

    private final static ConcurrentHashMap<String, Target> CHANNEL_TARGET_MAP = new ConcurrentHashMap<>();

    @Override
    public Mono<RpcPackage> request(Object data, Target target)
    {
        return request(data, target, config.getDefaultTaskTimeout());
    }

    @Override
    public Mono<RpcPackage> request(Object data, Target target, long timeout)
    {
        ChannelPool cacheChannelPool = CHANNEL_MAP.get(target.toString());
        if (cacheChannelPool != null){
            Channel one = cacheChannelPool.findOne();
            if (one != null && one.isActive()){
                RpcPackage msg = new RpcPackage();
                msg.setId(snowflake.nextId());
                msg.setRequest(EnumPackageType.REQUEST.getType());
                msg.setCode(EnumPackageCode.SUCCESS.getCode());
                msg.setValue(data);
                ChannelFuture channelFuture = one.writeAndFlush(msg);
                CompletableFuture<RpcPackage> responseFuture = new CompletableFuture<>();
                Mono<RpcPackage> responseMono = Mono.fromFuture(responseFuture);
                channelFuture.addListener((ChannelFutureListener) channelFuture1 ->
                {
                    if (channelFuture1.isSuccess()){
                        TaskManager.addTask(msg.getId(), responseFuture, timeout);
                    }else {
                        responseFuture.complete(RpcPackage.exceptionResponse(msg, new ChannelUnsupprotException()));
                    }
                });
                return responseMono;
            }
        }
        CompletableFuture<Channel> future = new CompletableFuture<>();
        bootstrap.connect(target.getHost(), target.getPort()).addListener(
                (ChannelFutureListener) channelFuture -> future.complete(channelFuture.channel())
        );
        Mono<Channel> channelMono = Mono.fromFuture(future);
        CompletableFuture<RpcPackage> responseFuture = new CompletableFuture<>();
        Mono<RpcPackage> responseMono = Mono.fromFuture(responseFuture);
        channelMono.subscribe(channel ->
        {
            RpcPackage msg = new RpcPackage();
            msg.setId(snowflake.nextId());
            msg.setRequest(EnumPackageType.REQUEST.getType());
            msg.setCode(EnumPackageCode.SUCCESS.getCode());
            msg.setValue(data);
            if (!channel.isActive()){
                responseFuture.complete(RpcPackage.exceptionResponse(msg, new TargetErrorException(target)));
            }
            AtomicBoolean check = new AtomicBoolean(true);
            ChannelPool channelPool = CHANNEL_MAP.computeIfAbsent(target.toString(), k -> {
                check.set(false);
                return new ChannelPool(target, channel);
            });
            CHANNEL_TARGET_MAP.putIfAbsent(channel.id().asLongText(), target);
            if (check.get()){
                channelPool.add(channel);
            }
            ChannelFuture channelFuture = channel.writeAndFlush(msg);
            channelFuture.addListener((ChannelFutureListener) channelFuture1 ->
            {
                if (channelFuture1.isSuccess()){
                    TaskManager.addTask(msg.getId(), responseFuture, timeout);
                }else {
                    responseFuture.complete(RpcPackage.exceptionResponse(msg, new ChannelUnsupprotException()));
                }
            });
        });
        return channelMono.then(responseMono);
    }

    public static void remove(Channel channel){
        Target target = CHANNEL_TARGET_MAP.remove(channel.id().asLongText());
        if (target == null){
            return ;
        }
        ChannelPool channelPool = CHANNEL_MAP.get(target.toString());
        if (channelPool == null){
            return ;
        }
        channelPool.remove(channel);
    }

    public static void printChannel(){
        CHANNEL_MAP.forEach((s, channelPool) ->
        {
            System.out.println(s);
            for (Channel channel : channelPool.channels)
            {
                System.out.println("==>" + channel.id().asLongText());
            }
        });
    }

}
