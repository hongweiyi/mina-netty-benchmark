package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;

import com.hongweiyi.bench.SimpleProtocol;
import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Netty4Client<T> extends Client<T> {
    protected boolean                netty4Pooled = false;
    private io.netty.channel.Channel client;
    private AbstractByteBufAllocator allocator;

    public Netty4Client(byte[] data) {
        this(data, Netty4TcpBenchmarkClient.NETTY4_ALLOC_UNPOOLED);
    }

    public Netty4Client(byte[] data, String netty4Pooled) {
        super(data);

        this.netty4Pooled = Netty4TcpBenchmarkClient.NETTY4_ALLOC_POOLED
            .equalsIgnoreCase(netty4Pooled);
        if (this.netty4Pooled) {
            allocator = PooledByteBufAllocator.DEFAULT;
        } else {
            allocator = UnpooledByteBufAllocator.DEFAULT;
        }
    }

    @Override
    public void send() {
        ByteBuf buf = allocator.buffer(data.length);
        buf.writeBytes(data);
        client.writeAndFlush(buf);
    }

    @Override
    public void send(long id) {
        super.send(id);
        ByteBuf buf = allocator.buffer(SimpleProtocol.HEADER_LENGTH + data.length);
        buf.writeInt(data.length);
        buf.writeLong(id);
        buf.writeBytes(data);

        client.writeAndFlush(buf);
    }

    @Override
    public void close() {
        client.disconnect();
        client.close();
    }

    @Override
    public void init(String host, int port, final T callbackPutObj) throws Exception {
        RecvCounterCallback clientCallback = new RecvCounterCallback() {
            @Override
            public void receive(long id) {
                try {
                    ArrayBlockingQueue<T> result = responses.get(id);
                    result.put(callbackPutObj);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        BenchmarkClient bcmClient = new Netty4TcpBenchmarkClient();
        this.client = (Channel) bcmClient.getInstance(host, port, clientCallback);
    }
}
