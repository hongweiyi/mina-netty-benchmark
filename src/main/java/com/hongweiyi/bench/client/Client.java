package com.hongweiyi.bench.client;

import java.util.concurrent.*;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public abstract class Client<T> {

    protected byte[]                                     data;
    protected ConcurrentMap<Long, ArrayBlockingQueue<T>> responses = new ConcurrentHashMap<Long, ArrayBlockingQueue<T>>(
                                                                       1024 * 20);

    public Client(byte[] data) {
        this.data = data;
    }

    public boolean put(long id) {
        ArrayBlockingQueue<T> responseQueue = new ArrayBlockingQueue<T>(1);
        responses.put(id, responseQueue);

        return true;
    }

    public boolean put(long id, T obj) {
        try {
            ArrayBlockingQueue<T> responseQueue = responses.get(id);
            responseQueue.put(obj);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public T poll(long id, long timeout, TimeUnit unit) {
        try {
            ArrayBlockingQueue<T> responseQueue = responses.get(id);
            T t = responseQueue.poll(timeout, unit);
            if (t != null) {
                responses.remove(id);
            }
            return t;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public abstract void send();

    public void send(long id) {
        put(id);
    }

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
