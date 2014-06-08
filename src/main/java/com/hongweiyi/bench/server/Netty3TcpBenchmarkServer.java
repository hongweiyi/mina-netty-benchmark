package com.hongweiyi.bench.server;

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

    private static final String CUMULATIN_ATTRIBUTE = Netty3TcpBenchmarkServer.class.getName()
                                                          + ".buffer";
    private static final String        LENGTH_ATTRIBUTE = Netty3TcpBenchmarkServer.class.getName()
                                                          + ".length";
    private static final ChannelBuffer ACK              = ChannelBuffers.buffer(1);

    static {
        ACK.writeByte(0);
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

    @Override
    public void start(int port) throws IOException {
        ChannelFactory factory = new NioServerSocketChannelFactory();
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setOption("receiveBufferSize", 64 * 1024);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new SimpleChannelUpstreamHandler() {
                    @Override
                    public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e)
                                                                                                     throws Exception {
                        System.out.println("childChannelOpen");
                        setAttribute(ctx, CUMULATIN_ATTRIBUTE, ChannelBuffers.buffer(64 * 1024));
                    }

                    @Override
                    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
                                                                                           throws Exception {
                        System.out.println("channelOpen");
                        setAttribute(ctx, CUMULATIN_ATTRIBUTE, ChannelBuffers.buffer(64 * 1024));
                        allChannels.add(ctx.getChannel());
                    }

                    @Override
                    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                                                                                          throws Exception {
                        if (e.getMessage() instanceof ChannelBuffer) {
                            ChannelBuffer buffer = (ChannelBuffer) e.getMessage();

                            ChannelBuffer cumulation;
                            if (getAttributesMap(ctx).containsKey(CUMULATIN_ATTRIBUTE)) {
                                cumulation = (ChannelBuffer) getAttribute(ctx, CUMULATIN_ATTRIBUTE);
                            } else {
                                cumulation = ChannelBuffers.buffer(64 * 1024);
                            }
                            int length = 0;
                            if (getAttributesMap(ctx).containsKey(LENGTH_ATTRIBUTE)) {
                                length = (Integer) getAttribute(ctx, LENGTH_ATTRIBUTE);
                            }

                            cumulation.writeBytes(buffer);

                            // I'm too lazy to write a independent DecodeHandler
                            // Aha... U can use {@link FrameDecoder}
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
                                    ctx.getChannel().write(ACK.slice());
                                } else if (length > remaining) { // body length less than expect length
                                    length -= remaining;
                                    cumulation.setIndex(readerIndex + remaining, cumulation.writerIndex());
                                } else if (length == remaining) {
                                    cumulation.setIndex(readerIndex + remaining, cumulation.writerIndex());
                                    length = 0;
                                    ctx.getChannel().write(ACK.slice());
                                } else if (length < remaining) {
                                    cumulation.setIndex(readerIndex + length, cumulation.writerIndex());
                                    length = 0;
                                    ctx.getChannel().write(ACK.slice());
                                }
                            }
                            if (cumulation.readableBytes() > 0) {
                                cumulation.clear();
                            } else {
                                cumulation.discardReadBytes();
                            }
                            setAttribute(ctx, CUMULATIN_ATTRIBUTE, cumulation);
                            setAttribute(ctx, LENGTH_ATTRIBUTE, length);
                        }
                    }

                    @Override
                    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
                                                                                             throws Exception {
                        allChannels.remove(ctx.getChannel());
                        System.out.println("channelClosed");
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
                                                                                            throws Exception {
                        e.getCause().printStackTrace();
                    }
                });
            }
        });
        allChannels.add(bootstrap.bind(new InetSocketAddress(port)));

    }

    @Override
    public void stop() throws IOException {
    }

}
