package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;

/**
 * @author hongwei.yhw
 * @version 2014-03-01. 9:03 PM
 */
public abstract class BenchmarkClient {

    /**
     *
     * get netty or mina client
     *
     * @param host: connect host
     * @param port: connect port
     * @param clientCallback: client notification
     * @return netty: Channel object, mina:IoSession object
     * @throws Exception
     */
    public abstract Object getInstance(String host, int port,
                                       final RecvCounterCallback clientCallback, String... params)
                                                                                                  throws Exception;
}
