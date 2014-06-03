package com.hongweiyi.bench.client;

import org.apache.mina.api.IoSession;

import java.nio.ByteBuffer;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public class Mina3Client extends Client {

    IoSession client;

    public Mina3Client(byte[] data, Object client) {
        super(data);
        this.client = (IoSession) client;
    }

    public void send() {
        client.write(ByteBuffer.wrap(data));
    }

    public void close() {
        client.close(true);
    }
}
