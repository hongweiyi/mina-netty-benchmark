package com.hongweiyi.bench.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public abstract class Client<T> {

    protected byte[]           data;
    protected BlockingQueue<T> blockingQueue = new ArrayBlockingQueue<T>(1024 * 2);

    public Client(byte[] data) {
        this.data = data;
    }

    public boolean put(T obj) {
        try {
            blockingQueue.put(obj);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public T poll(long timeout, TimeUnit unit) {
        try {
            return blockingQueue.poll(timeout, unit);
        } catch (InterruptedException e) {
            return null;
        }

    }

    public abstract void send();

    public abstract void close();

    /**
     * init the real netty/mina client
     * 
     * @param host: server host
     * @param port: server port
     * @param callbackPutObj: when client receive response,
     *                      callback will put this obj to blockingQueue to notify biz thread
     * @throws Exception
     */
    public abstract void init(String host, int port, T callbackPutObj) throws Exception;
}
