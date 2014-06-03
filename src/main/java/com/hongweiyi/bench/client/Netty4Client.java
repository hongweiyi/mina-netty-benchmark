package com.hongweiyi.bench.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Netty4Client extends Client {
    protected boolean                netty4Pooled = false;
    private io.netty.channel.Channel client;

    public Netty4Client(byte[] data, Object client) {
        this(data, client, Netty4TcpBenchmarkClient.NETTY4_ALLOC_UNPOOLED);
    }

    public Netty4Client(byte[] data, Object client, String netty4Pooled) {
        super(data);
        this.client = (Channel) client;

        this.netty4Pooled = Netty4TcpBenchmarkClient.NETTY4_ALLOC_POOLED
            .equalsIgnoreCase(netty4Pooled);
    }

    public void send() {
        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(data.length);
        buf.writeBytes(data);
        client.writeAndFlush(buf);
    }

    public void close() {
        client.disconnect();
        client.close();
    }
}
