package com.hongweiyi.bench.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Netty4Client extends Client {
    protected boolean netty4Pooled = false;

    public Netty4Client(byte[] data, Object client) {
        this(data, client, Netty4TcpBenchmarkClient.NETTY4_ALLOC_UNPOOLED);
    }

    public Netty4Client(byte[] data, Object client, String netty4Pooled) {
        super(data, client);
        this.netty4Pooled = Netty4TcpBenchmarkClient.NETTY4_ALLOC_POOLED
            .equalsIgnoreCase(netty4Pooled);
    }

    public void send() {
        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(data.length);
        buf.writeBytes(data);
        ((io.netty.channel.Channel) client).writeAndFlush(buf);
    }

    public void close() {
        ((io.netty.channel.Channel) client).disconnect();
        ((io.netty.channel.Channel) client).close();
    }
}
