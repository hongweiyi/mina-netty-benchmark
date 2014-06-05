package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Netty4Client<T> extends Client<T> {
    protected boolean                netty4Pooled = false;
    private io.netty.channel.Channel client;

    public Netty4Client(byte[] data) {
        this(data, Netty4TcpBenchmarkClient.NETTY4_ALLOC_UNPOOLED);
    }

    public Netty4Client(byte[] data, String netty4Pooled) {
        super(data);

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

    @Override
    public void init(String host, int port, final T callbackPutObj) throws Exception {
        RecvCounterCallback clientCallback = new RecvCounterCallback() {
            @Override
            public void receive() {
                try {
                    blockingQueue.put(callbackPutObj);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        BenchmarkClient bcmClient = new Netty4TcpBenchmarkClient();
        this.client = (Channel) bcmClient.getInstance(host, port, clientCallback);
    }
}
