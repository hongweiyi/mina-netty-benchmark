package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 8:16 PM
 */
public class Netty3TcpBenchmarkClient extends BenchmarkClient {
    private static final String CUMULATIN_ATTRIBUTE = Netty3TcpBenchmarkClient.class.getName()
                                                      + ".buffer";

    @Override
    public Object getInstance(String host, int port, final RecvCounterCallback clientCallback,
                              String... params) throws Exception {
        ChannelFactory factory = new NioClientSocketChannelFactory();

        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setOption("sendBufferSize", 64 * 1024);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new SimpleChannelUpstreamHandler() {

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
                            cumulation.writeBytes(buffer);

                            while (cumulation.readableBytes() >= 8) {
                                long id = cumulation.readLong();
                                clientCallback.receive(id);
                            }

                            if (cumulation.readableBytes() == 0) {
                                cumulation.clear();
                            } else {
                                cumulation.discardReadBytes();
                            }
                            setAttribute(ctx, CUMULATIN_ATTRIBUTE, cumulation);
                        } else {
                            throw new IllegalArgumentException(e.getMessage().getClass().getName());
                        }
                    }

                });
            }
        });

        return bootstrap.connect(new InetSocketAddress(host, port)).sync().getChannel();
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
}
