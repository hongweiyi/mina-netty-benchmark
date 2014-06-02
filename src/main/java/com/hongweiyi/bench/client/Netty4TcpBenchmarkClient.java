package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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

    public static final String LABEL                 = "Netty4";
    public static final String NETTY4_ALLOC_POOLED   = "pooled";
    public static final String NETTY4_ALLOC_UNPOOLED = "unpooled";
    private String             netty4AllocMethod;

    private EventLoopGroup     group                 = new NioEventLoopGroup();

    @Override
    public Object getClient(int port, final RecvCounterCallback clientCallback,
                            final String... params) throws Exception {
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
                        netty4AllocMethod = NETTY4_ALLOC_POOLED;
                    } else if (NETTY4_ALLOC_UNPOOLED.equals(param)) {
                        ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                        netty4AllocMethod = NETTY4_ALLOC_UNPOOLED;
                    }
                }
                if (null == netty4AllocMethod) {
                    netty4AllocMethod = NETTY4_ALLOC_UNPOOLED;
                }

                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                    @Override
                    public void channelRead(io.netty.channel.ChannelHandlerContext ctx,
                                            Object message) throws Exception {
                        if (message instanceof ByteBuf) {
                            ByteBuf buffer = (ByteBuf) message;
                            long length = buffer.readableBytes();
                            while (length-- > 0) { // server responses only one byte
                                clientCallback.receive();
                            }
                        } else {
                            throw new IllegalArgumentException(message.getClass().getName());
                        }
                    }

                    @Override
                    public void channelActive(io.netty.channel.ChannelHandlerContext ctx)
                                                                                         throws Exception {
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
        return LABEL + "-" + netty4AllocMethod + "-" + super.getLabel();
    }
}
