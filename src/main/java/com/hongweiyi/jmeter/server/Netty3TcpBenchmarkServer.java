package com.hongweiyi.jmeter.server;


import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 7:52 PM
 */
public class Netty3TcpBenchmarkServer extends BenchmarkServer {

    public static final String LABEL = "Netty3-Server";

    private static final String STATE_ATTRIBUTE = Netty3TcpBenchmarkServer.class.getName() + ".state";
    private static final String LENGTH_ATTRIBUTE = Netty3TcpBenchmarkServer.class.getName() + ".length";
    private static final ChannelBuffer ACK = ChannelBuffers.buffer(1);

    static {
        ACK.writeByte(0);
    }

    private static enum State {
        WAIT_FOR_FIRST_BYTE_LENGTH, WAIT_FOR_SECOND_BYTE_LENGTH, WAIT_FOR_THIRD_BYTE_LENGTH, WAIT_FOR_FOURTH_BYTE_LENGTH, READING
    }
    protected static Map<String, Object> getAttributesMap(ChannelHandlerContext ctx) {
        Map<String, Object> map = (Map<String, Object>) ctx.getAttachment();
        if (map == null) {
            map = new HashMap<String, Object>();
            ctx.setAttachment(map);
        }
        return map;
    }
    private static void setAttribute(ChannelHandlerContext ctx, String name, Object value) {
        getAttributesMap(ctx).put(name, value);
    }

    private static Object getAttribute(ChannelHandlerContext ctx, String name) {
        return getAttributesMap(ctx).get(name);
    }
    private static ChannelGroup allChannels = new DefaultChannelGroup();

    private ChannelFactory factory;

    @Override
    public void start(int port) throws IOException {
        factory = new NioServerSocketChannelFactory();
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setOption("receiveBufferSize", 64 * 1024);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new SimpleChannelUpstreamHandler() {
                    @Override
                    public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
                        System.out.println("childChannelOpen");
                        setAttribute(ctx, STATE_ATTRIBUTE, State.WAIT_FOR_FIRST_BYTE_LENGTH);
                    }

                    @Override
                    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                        System.out.println("channelOpen");
                        setAttribute(ctx, STATE_ATTRIBUTE, State.WAIT_FOR_FIRST_BYTE_LENGTH);
                        allChannels.add(ctx.getChannel());
                    }


                    @Override
                    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                        if (e.getMessage() instanceof ChannelBuffer) {
                            ChannelBuffer buffer = (ChannelBuffer) e.getMessage();

                            State state = (State) getAttribute(ctx, STATE_ATTRIBUTE);
                            int length = 0;
                            if (getAttributesMap(ctx).containsKey(LENGTH_ATTRIBUTE)) {
                                length = (Integer) getAttribute(ctx, LENGTH_ATTRIBUTE);
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
                                            ctx.getChannel().write(ACK.slice());
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
                                            ctx.getChannel().write(ACK.slice());
                                            state = State.WAIT_FOR_FIRST_BYTE_LENGTH;
                                            length = 0;
                                        }
                                }
                            }
                            setAttribute(ctx, STATE_ATTRIBUTE, state);
                            setAttribute(ctx, LENGTH_ATTRIBUTE, length);
                        }
                    }

                    @Override
                    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                        allChannels.remove(ctx.getChannel());
                        System.out.println("channelClosed");
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
                        e.getCause().printStackTrace();
                    }
                });
            }
        });
        allChannels.add(bootstrap.bind(new InetSocketAddress(port)));

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
