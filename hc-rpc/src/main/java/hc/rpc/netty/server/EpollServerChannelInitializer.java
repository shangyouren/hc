package hc.rpc.netty.server;

import hc.rpc.netty.encoder.NetworkPackageDecoder;
import hc.rpc.netty.encoder.NetworkPackageEncoder;
import hc.rpc.service.RpcServerFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EpollServerChannelInitializer extends ChannelInitializer<EpollSocketChannel>
{

    private final RpcServerFactory factory;

    @Override
    protected void initChannel(EpollSocketChannel socketChannel) throws Exception
    {
        socketChannel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(8 * 1024 * 1024, 0, 4))
                .addLast(new NetworkPackageDecoder<>())
                .addLast(new NetworkPackageEncoder<>())
                .addLast(new ServerChannelHandler(factory.newServerChannel()));
    }
}
