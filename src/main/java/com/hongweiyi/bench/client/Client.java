package com.hongweiyi.bench.client;

import com.hongweiyi.bench.ResponseFuture;

import java.util.concurrent.*;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:18 PM
 */
public abstract class Client<T> {

    protected byte[]                                 data;
    protected ConcurrentMap<Long, ResponseFuture<T>> responses = new ConcurrentHashMap<Long, ResponseFuture<T>>(
                                                                   1024 * 20);

    public Client(byte[] data) {
        this.data = data;
    }

    public boolean put(long id) {
        ResponseFuture<T> future = new ResponseFuture<T>();
        responses.put(id, future);

        return true;
    }

    public boolean put(long id, T obj) {
        try {
            ResponseFuture<T> future = responses.get(id);
            future.handleResponse(obj);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public T poll(long id, long timeout, TimeUnit unit) {
        try {
            ResponseFuture<T> future = responses.get(id);
            T t = future.get(timeout, unit);
            if (t != null) {
                responses.remove(id);
            }
            return t;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
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
