package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;
import com.hongweiyi.bench.ResponseFuture;
import com.hongweiyi.bench.SimpleProtocol;
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
    public void send(long id) {
        super.send(id);
        ByteBuffer byteBuffer = ByteBuffer.allocate(SimpleProtocol.HEADER_LENGTH + data.length);
        byteBuffer.putInt(data.length);
        byteBuffer.putLong(id);
        byteBuffer.put(data);

        byteBuffer.flip();

        client.write(byteBuffer);
    }

    @Override
    public void close() {
        client.close(true);
    }

    @Override
    public void init(String host, int port, final T callbackPutObj) throws Exception {
        RecvCounterCallback clientCallback = new RecvCounterCallback() {
            @Override
            public void receive(long id) {
                try {
                    ResponseFuture<T> result = responses.get(id);
                    result.handleResponse(callbackPutObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        BenchmarkClient bcmClient = new Mina3TcpBenchmarkClient();
        this.client = (IoSession) bcmClient.getInstance(host, port, clientCallback);
    }

}
