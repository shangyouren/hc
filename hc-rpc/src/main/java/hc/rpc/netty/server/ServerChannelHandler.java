package hc.rpc.netty.server;

import hc.rpc.errors.ChannelUnsupprotException;
import hc.rpc.pojo.EnumPackageCode;
import hc.rpc.pojo.EnumPackageType;
import hc.rpc.pojo.RpcPackage;
import hc.rpc.service.RpcServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;


@AllArgsConstructor
public class ServerChannelHandler extends ChannelInboundHandlerAdapter
{

    public final RpcServer server;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        RpcPackage rpcPackage = (RpcPackage) msg;
        Mono<RpcPackage> accept = server.accept(rpcPackage);
        accept.doOnError(throwable ->
        {
            if (!ctx.channel().isActive())
            {
                throw new ChannelUnsupprotException();
            }
            RpcPackage response = new RpcPackage();
            response.setException(throwable);
            response.setId(rpcPackage.getId());
            response.setCode(EnumPackageCode.ERROR.getCode());
            response.setRequest(EnumPackageType.RESPONSE.getType());
            ctx.writeAndFlush(response);
        });
        accept.subscribe(response ->
        {
            if (!ctx.channel().isActive()){
                throw new ChannelUnsupprotException();
            }
            response.setId(rpcPackage.getId());
            response.setRequest(EnumPackageType.RESPONSE.getType());
            response.setRequest(EnumPackageType.RESPONSE.getType());
            ctx.writeAndFlush(response);
        });
    }
}
