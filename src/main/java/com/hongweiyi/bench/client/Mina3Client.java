package com.hongweiyi.bench.client;

import org.apache.mina.api.IoSession;

import java.nio.ByteBuffer;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Mina3Client extends Client {

    public Mina3Client(byte[] data, Object client) {
        super(data, client);
    }

    public void send() {
        ((IoSession) client).write(ByteBuffer.wrap(data));
    }

    public void close() {
        ((IoSession) client).close(true);
    }
}
