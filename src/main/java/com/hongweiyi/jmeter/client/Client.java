package com.hongweiyi.jmeter.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.apache.mina.api.IoSession;
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
    private Object      data;

    private Object      client;

    public Client(CLIENT_TYPE type, byte[] data, Object client) {
        if (null == type || null == data || null == client) {
            return;
        }

        this.type = type;
        this.data = data;
        this.client = client;
    }

    public void send() {
        if (type.equals(CLIENT_TYPE.NETTY3)) {
            ((Channel) client).write(ChannelBuffers.wrappedBuffer((byte[])data));
        } else if (type.equals(CLIENT_TYPE.MINA3)) {
            ((IoSession) client).write(ByteBuffer.wrap((byte[]) data));
        } else if (type.equals(CLIENT_TYPE.NETTY4)) {
            ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(((byte[]) data).length);
            buf.writeBytes((byte[]) data);
            ((io.netty.channel.Channel) client).writeAndFlush(buf);
        }
    }

    public void close() {
        if (type.equals(CLIENT_TYPE.NETTY3)) {
            ((Channel) client).close();
        } else if (type.equals(CLIENT_TYPE.MINA3)) {
            ((IoSession) client).close(true);
        }
    }
}
