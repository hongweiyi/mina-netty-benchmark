package com.hongweiyi.jmeter.client;


import com.hongweiyi.jmeter.RecvCounterCallback;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 8:16 PM
 */
public class Netty4TcpBenchmarkClient extends BenchmarkClient {

    public static final String LABEL = "Netty4";
    public static final String NETTY4_ALLOC_POOLED = "pooled";
    public static final String NETTY4_ALLOC_UNPOOLED = "unpooled";

    private EventLoopGroup group = new NioEventLoopGroup();


    @Override
    public Object getClient(int port, final RecvCounterCallback clientCallback, final String ... params)
            throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.option(ChannelOption.SO_SNDBUF, 64 * 10 * 1024);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                for (String param : params) {
                    if (NETTY4_ALLOC_POOLED.equals(param)) {
                        ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                    } else if (NETTY4_ALLOC_UNPOOLED.equals(param)) {
                        ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                    }
                }
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                    @Override
                    public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object message) throws Exception {
                        clientCallback.receive();
                    }

                    @Override
                    public void channelActive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
                    }
                });
            }

            @Override
            public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                ctx.close();
            }
        });
        return bootstrap.connect(new InetSocketAddress(port)).sync().channel();
    }

    @Override
    public String getLabel() {
        return LABEL + " -> " + super.getLabel();
    }
}
