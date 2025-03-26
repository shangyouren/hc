package hc.rpc;


import hc.rpc.netty.client.EpollClientChannelInitializer;
import hc.rpc.netty.client.NioClientChannelInitializer;
import hc.rpc.netty.server.EpollServerChannelInitializer;
import hc.rpc.netty.server.NioServerChannelInitializer;
import hc.rpc.service.RpcClient;
import hc.rpc.service.RpcServerFactory;
import hc.rpc.service.impl.NettyRpcClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class Bootstrap {

    private final EventLoopGroup bossGroup =
            Epoll.isAvailable() ?
                    new EpollEventLoopGroup(1) :
                    new NioEventLoopGroup(1);

    private final EventLoopGroup workGroup =
            Epoll.isAvailable() ?
                    new EpollEventLoopGroup(4) :
                    new NioEventLoopGroup(4);


    public void startServer(RpcServerFactory factory)
    {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 8 * 1024)
                .childOption(ChannelOption.SO_RCVBUF, 8 * 1024)
                .localAddress(new InetSocketAddress(9005))
                .childHandler(Epoll.isAvailable() ? new EpollServerChannelInitializer(factory) : new NioServerChannelInitializer(factory));
        serverBootstrap.bind(9005);
    }

    public RpcClient startClient(){
        io.netty.bootstrap.Bootstrap bootstrap = new io.netty.bootstrap.Bootstrap();
        bootstrap.group(workGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioSocketChannel.class)
                .handler(Epoll.isAvailable() ? new EpollClientChannelInitializer() : new NioClientChannelInitializer());
        return new NettyRpcClient(bootstrap);
    }

}
