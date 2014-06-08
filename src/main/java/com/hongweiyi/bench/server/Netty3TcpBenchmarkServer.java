package com.hongweiyi.bench.server;

import com.hongweiyi.bench.SimpleProtocol;
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
    private static final String LENGTH_ATTRIBUTE    = Netty3TcpBenchmarkServer.class.getName()
                                                      + ".length";
    private static final String ID_ATTRIBUTE        = Netty3TcpBenchmarkServer.class.getName()
                                                      + ".id";

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
                            long id = 0;
                            if (getAttributesMap(ctx).containsKey(LENGTH_ATTRIBUTE)) {
                                length = (Integer) getAttribute(ctx, LENGTH_ATTRIBUTE);
                            }
                            if (getAttributesMap(ctx).containsKey(ID_ATTRIBUTE)) {
                                id = (Long) getAttribute(ctx, ID_ATTRIBUTE);
                            }

                            cumulation.writeBytes(buffer);

                            // I'm too lazy to write a independent DecodeHandler
                            // Aha... U can use {@link FrameDecoder}
                            while (cumulation.readableBytes() > 0) {
                                int remaining = cumulation.readableBytes();
                                if (length == 0) { // has nothing more to read
                                    if (remaining >= SimpleProtocol.HEADER_LENGTH) {
                                        length = cumulation.readInt();
                                        id = cumulation.readLong();
                                        remaining = cumulation.readableBytes();
                                    } else {
                                        break; // remaining data cannot satisfied header length demand
                                    }
                                }

                                int readerIndex = cumulation.readerIndex();

                                if ((length == 0)) { // only header, no body data
                                    write(ctx, id);
                                } else if (length > remaining) { // body length less than expect length
                                    length -= remaining;
                                    cumulation.setIndex(readerIndex + remaining,
                                        cumulation.writerIndex());
                                } else if (length == remaining) {
                                    cumulation.setIndex(readerIndex + remaining,
                                        cumulation.writerIndex());
                                    length = 0;
                                    write(ctx, id);
                                } else if (length < remaining) {
                                    cumulation.setIndex(readerIndex + length,
                                        cumulation.writerIndex());
                                    length = 0;
                                    write(ctx, id);
                                }
                            }
                            if (cumulation.readableBytes() == 0) {
                                cumulation.clear();
                            } else {
                                cumulation.discardReadBytes();
                            }
                            setAttribute(ctx, CUMULATIN_ATTRIBUTE, cumulation);
                            setAttribute(ctx, LENGTH_ATTRIBUTE, length);
                            setAttribute(ctx, ID_ATTRIBUTE, id);
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

    private void write(ChannelHandlerContext ctx, long id) {
        ChannelBuffer buffer = ChannelBuffers.buffer(8);
        buffer.writeLong(id);
        ctx.getChannel().write(buffer);
    }
}
