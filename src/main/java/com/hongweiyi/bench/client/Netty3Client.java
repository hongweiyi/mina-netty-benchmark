package com.hongweiyi.bench.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Netty3Client extends Client {

    public Netty3Client(byte[] data, Object client) {
        super(data, client);
    }

    public void send() {
        ChannelBuffer buf = ChannelBuffers.buffer(data.length);
        buf.writeBytes(data);
        ((Channel) client).write(buf);
    }

    public void close() {
        ((Channel) client).disconnect();
        ((Channel) client).close();
    }
}
