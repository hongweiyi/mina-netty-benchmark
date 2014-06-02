package com.hongweiyi.bench.client;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public abstract class Client {

    protected byte[] data;

    protected Object client;

    public Client(byte[] data, Object client) {
        if (null == data || null == client) {
            return;
        }

        this.data = data;
        this.client = client;
    }

    public abstract void send();

    public abstract void close();

}
