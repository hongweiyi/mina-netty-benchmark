package com.hongweiyi.jmeter.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.apache.mina.api.IoSession;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import java.nio.ByteBuffer;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Client {
    public static enum CLIENT_TYPE {
        MINA2, MINA3, NETTY3, NETTY4
    }

    private CLIENT_TYPE type;
    private byte[]      data;

    private Object      client;
    private boolean     netty4Pooled;

    public Client(CLIENT_TYPE type, byte[] data, Object client) {
        this(type, data, client, Netty4TcpBenchmarkClient.NETTY4_ALLOC_UNPOOLED);
    }
    public Client(CLIENT_TYPE type, byte[] data, Object client, String netty4Pooled) {
        if (null == type || null == data || null == client) {
            return;
        }

        this.type = type;
        this.data = data;
        this.client = client;
        this.netty4Pooled = Netty4TcpBenchmarkClient.NETTY4_ALLOC_POOLED.equalsIgnoreCase(netty4Pooled);
    }

    public void send() {
        if (type.equals(CLIENT_TYPE.MINA3)) {
            ((IoSession) client).write(ByteBuffer.wrap(data));
        } else if (type.equals(CLIENT_TYPE.NETTY3)) {
            ChannelBuffer buf = ChannelBuffers.buffer(data.length);
            buf.writeBytes(data);
            ((Channel) client).write(buf);
        } else if (type.equals(CLIENT_TYPE.NETTY4)) {
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(data.length);
            buf.writeBytes(data);
            ((io.netty.channel.Channel) client).writeAndFlush(buf);
        }
    }

    public void close() {
        if (type.equals(CLIENT_TYPE.MINA3)) {
            ((IoSession) client).close(true);
        } else if (type.equals(CLIENT_TYPE.NETTY3)) {
            ((Channel) client).close();
        } else if (type.equals(CLIENT_TYPE.NETTY4)) {
            ((io.netty.channel.Channel)client).close();
        }
    }
}
