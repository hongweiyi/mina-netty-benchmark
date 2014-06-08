package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;
import com.hongweiyi.bench.SimpleProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Netty3Client<T> extends Client<T> {

    private Channel client;

    public Netty3Client(byte[] data) {
        super(data);
    }

    @Override
    public void send() {
        ChannelBuffer buf = ChannelBuffers.buffer(data.length);
        buf.writeBytes(data);
        client.write(buf);
    }

    @Override
    public void send(long id) {
        super.send(id);
        ChannelBuffer buf = ChannelBuffers.buffer(SimpleProtocol.HEADER_LENGTH + data.length);
        buf.writeInt(data.length);
        buf.writeLong(id);
        buf.writeBytes(data);

        client.write(buf);
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
        BenchmarkClient bcmClient = new Netty3TcpBenchmarkClient();
        this.client = (Channel) bcmClient.getInstance(host, port, clientCallback);
    }
}
