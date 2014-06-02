package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hongwei.yhw
 * @version 2014-03-01. 9:03 PM
 */
public abstract class BenchmarkClient {

    private static AtomicInteger id = new AtomicInteger(0);

    public String getLabel() {
        int i = id.get();
        id.incrementAndGet();
        return "Id: " + i;
    }

    /**
     *
     * get netty or mina client
     *
     * @param port: connected port
     * @param clientCallback: client notification
     * @return netty: Channel object mina:IoSession object
     * @throws Exception
     */
    public abstract Object getClient(int port, final RecvCounterCallback clientCallback,
                                     String... params) throws Exception;
}
