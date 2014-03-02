package com.hongweiyi.jmeter.client;


import com.hongweiyi.jmeter.RecvCounterCallback;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 8:16 PM
 */
public class Netty3TcpBenchmarkClient extends BenchmarkClient {

    public static final String LABEL = "Netty3";

    @Override
    public Object getClient(int port, final RecvCounterCallback clientCallback, String ... params)
            throws Exception {
        ChannelFactory factory = new NioClientSocketChannelFactory();

        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setOption("sendBufferSize", 64 * 1024);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new SimpleChannelUpstreamHandler() {

                    @Override
                    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                        if (e.getMessage() instanceof ChannelBuffer) {
                            clientCallback.receive();
                        } else {
                            throw new IllegalArgumentException(e.getMessage().getClass().getName());
                        }
                    }

                });
            }
        });

        return bootstrap.connect(new InetSocketAddress(port)).sync().getChannel();
    }

    @Override
    public String getLabel() {
        return LABEL + " -> " + super.getLabel();
    }
}
