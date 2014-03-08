package com.hongweiyi.jmeter.server;


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

    public static final String LABEL = "Netty4-Server";

    private static enum State {
        WAIT_FOR_FIRST_BYTE_LENGTH, WAIT_FOR_SECOND_BYTE_LENGTH, WAIT_FOR_THIRD_BYTE_LENGTH, WAIT_FOR_FOURTH_BYTE_LENGTH, READING
    }

    private static final ByteBuf ACK = Unpooled.buffer(1);

    static {
        ACK.writeByte(0);
    }

    private static final AttributeKey<State> STATE_ATTRIBUTE = new AttributeKey<State>("state");

    private static final AttributeKey<Integer> LENGTH_ATTRIBUTE = new AttributeKey<Integer>("length");

    private io.netty.bootstrap.ServerBootstrap bootstrap = null;

    private class TestServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelReadComplete(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        }

        public void channelRegistered(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
            System.out.println("childChannelOpen");
            ctx.attr(STATE_ATTRIBUTE).set(State.WAIT_FOR_FIRST_BYTE_LENGTH);
        }

        @Override
        public void channelUnregistered(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        }

        @Override
        public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }

        @Override
        public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object message) throws Exception {
            ByteBuf buffer = (ByteBuf)message;
            State state = ctx.attr(STATE_ATTRIBUTE).get();
            int length = 0;
            Attribute<Integer> lengthAttribute = ctx.attr(LENGTH_ATTRIBUTE);

            if (lengthAttribute.get() != null) {
                length = lengthAttribute.get();
            }

            while (buffer.readableBytes() > 0) {
                switch (state) {
                    case WAIT_FOR_FIRST_BYTE_LENGTH:
                        length = (buffer.readByte() & 255) << 24;
                        state = State.WAIT_FOR_SECOND_BYTE_LENGTH;
                        break;

                    case WAIT_FOR_SECOND_BYTE_LENGTH:
                        length += (buffer.readByte() & 255) << 16;
                        state = State.WAIT_FOR_THIRD_BYTE_LENGTH;
                        break;

                    case WAIT_FOR_THIRD_BYTE_LENGTH:
                        length += (buffer.readByte() & 255) << 8;
                        state = State.WAIT_FOR_FOURTH_BYTE_LENGTH;
                        break;

                    case WAIT_FOR_FOURTH_BYTE_LENGTH:
                        length += (buffer.readByte() & 255);
                        state = State.READING;

                        if ((length == 0) && (buffer.readableBytes() == 0)) {
                            ctx.writeAndFlush(ACK.retain(1).resetReaderIndex());
                            state = State.WAIT_FOR_FIRST_BYTE_LENGTH;
                        }

                        break;

                    case READING:
                        int remaining = buffer.readableBytes();

                        if (length > remaining) {
                            length -= remaining;
                            buffer.skipBytes(remaining);
                        } else {
                            buffer.skipBytes(length);
                            ctx.writeAndFlush(ACK.retain(1).resetReaderIndex());
                            state = State.WAIT_FOR_FIRST_BYTE_LENGTH;
                            length = 0;
                        }
                }
            }

            ctx.attr(STATE_ATTRIBUTE).set(state);
            ctx.attr(LENGTH_ATTRIBUTE).set(length);
            buffer.release();
        }
    }

    @Override
    public void start(int port) throws IOException {
        try {
            bootstrap = new io.netty.bootstrap.ServerBootstrap();
            bootstrap.option(ChannelOption.SO_RCVBUF, 128 * 1024);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);

            bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup());
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(port);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    channel.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                    channel.pipeline().addLast(new TestServerHandler());
                };
            });
            bootstrap.bind();
        } finally {
        }
    }

    @Override
    public void stop() throws IOException {
        // do nothing
        // Can not close the server
        // Server is a static object
        // so Server will last until class is unloaded
    }

    @Override
    public String getLabel() {
        return LABEL;
    }
}
