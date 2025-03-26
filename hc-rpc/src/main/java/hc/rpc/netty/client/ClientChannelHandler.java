package hc.rpc.netty.client;

import hc.rpc.pojo.RpcPackage;
import hc.rpc.service.TaskManager;
import hc.rpc.service.impl.NettyRpcClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;


@AllArgsConstructor
public class ClientChannelHandler extends ChannelInboundHandlerAdapter
{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        RpcPackage rpcPackage = (RpcPackage) msg;
        CompletableFuture<RpcPackage> future = TaskManager.getAndRemoveResponseListener(rpcPackage.getId());
        if (future == null){
            return ;
        }
        future.complete(rpcPackage);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        NettyRpcClient.remove(ctx.channel());
    }
}
