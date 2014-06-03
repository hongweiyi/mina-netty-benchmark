package com.hongweiyi.bench.client;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public abstract class Client {

    protected byte[] data;

    public Client(byte[] data) {
        this.data = data;
    }

    public abstract void send();

    public abstract void close();

}
