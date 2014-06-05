package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;
import org.apache.mina.api.IoSession;

import java.nio.ByteBuffer;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Mina3Client<T> extends Client<T> {

    private IoSession client;

    public Mina3Client(byte[] data) {
        super(data);
    }

    @Override
    public void send() {
        client.write(ByteBuffer.wrap(data));
    }

    @Override
    public void close() {
        client.close(true);
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
        BenchmarkClient bcmClient = new Mina3TcpBenchmarkClient();
        this.client = (IoSession) bcmClient.getInstance(host, port, clientCallback);
    }

}
