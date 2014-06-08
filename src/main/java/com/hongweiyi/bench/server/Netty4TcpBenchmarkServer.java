package com.hongweiyi.bench.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.IOException;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 7:52 PM
 */
public class Netty4TcpBenchmarkServer extends BenchmarkServer {

    private static final ByteBuf               ACK              = Unpooled.buffer(1);

    static {
        ACK.writeByte(0);
    }

    private static final AttributeKey<ByteBuf> CUMULATION_ATTRIBUTE = new AttributeKey<ByteBuf>("buffer");

    private static final AttributeKey<Integer> LENGTH_ATTRIBUTE = new AttributeKey<Integer>(
                                                                    "length");

    private io.netty.bootstrap.ServerBootstrap bootstrap        = null;

    private class TestServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelReadComplete(io.netty.channel.ChannelHandlerContext ctx)
                                                                                   throws Exception {
        }

        public void channelRegistered(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
            System.out.println("childChannelOpen");
            ctx.attr(CUMULATION_ATTRIBUTE).set(UnpooledByteBufAllocator.DEFAULT.buffer(64 * 1024));
        }

        @Override
        public void channelUnregistered(io.netty.channel.ChannelHandlerContext ctx)
                                                                                   throws Exception {
        }

        @Override
        public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause)
                                                                                                throws Exception {
            cause.printStackTrace();
        }

        @Override
        public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object message)
                                                                                           throws Exception {
            ByteBuf buffer = (ByteBuf) message;
            int length = 0;
            ByteBuf cumulation;
            Attribute<ByteBuf> cumulationAttribute = ctx.attr(CUMULATION_ATTRIBUTE);
            Attribute<Integer> lengthAttribute = ctx.attr(LENGTH_ATTRIBUTE);

            if (lengthAttribute.get() != null) {
                length = lengthAttribute.get();
            }
            if (cumulationAttribute.get() != null) {
                cumulation = cumulationAttribute.get();
            } else {
                cumulation = UnpooledByteBufAllocator.DEFAULT.buffer(64 * 1024);
            }

            cumulation.writeBytes(buffer);

            // I'm too lazy to write a independent DecodeHandler
            // Aha... U can use {@link ByteToMessageDecoder}
            while (cumulation.readableBytes() > 0) {
                int remaining = cumulation.readableBytes();
                if (length == 0) { // has nothing more to read
                    if (remaining >= 4) {
                        length = (cumulation.readByte() & 255) << 24;
                        length += (cumulation.readByte() & 255) << 16;
                        length += (cumulation.readByte() & 255) << 8;
                        length += (cumulation.readByte() & 255);
                        remaining = cumulation.readableBytes();
                    } else {
                        break; // remaining data cannot satisfied header length demand
                    }
                }

                int readerIndex = cumulation.readerIndex();

                if ((length == 0)) { // only header, no body data
                    ctx.writeAndFlush(ACK.retain(1).resetReaderIndex());
                } else if (length > remaining) { // body length less than expect length
                    length -= remaining;
                    cumulation.setIndex(readerIndex + remaining, cumulation.writerIndex());
                } else if (length == remaining) {
                    cumulation.setIndex(readerIndex + remaining, cumulation.writerIndex());
                    length = 0;
                    ctx.writeAndFlush(ACK.retain(1).resetReaderIndex());
                } else if (length < remaining) {
                    cumulation.setIndex(readerIndex + length, cumulation.writerIndex());
                    length = 0;
                    ctx.writeAndFlush(ACK.retain(1).resetReaderIndex());
                }
            }
            if (cumulation.readableBytes() > 0) {
                cumulation.clear();
            } else {
                cumulation.discardReadBytes();
            }

            ctx.attr(CUMULATION_ATTRIBUTE).set(cumulation);
            ctx.attr(LENGTH_ATTRIBUTE).set(length);
            buffer.release();
        }
    }

    @Override
    public void start(int port) throws IOException {
        try {
            bootstrap = new io.netty.bootstrap.ServerBootstrap();
            bootstrap.option(ChannelOption.SO_RCVBUF, 64 * 1024);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);

            bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup());
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(port);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    channel.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                    channel.pipeline().addLast(new TestServerHandler());
                }
            });
            bootstrap.bind();
        } finally {
        }
    }

    @Override
    public void stop() throws IOException {
    }

}
