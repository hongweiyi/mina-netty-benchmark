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
     * @param port: connect port
     * @param host: connect host
     * @param clientCallback: client notification
     * @return netty: Channel object, mina:IoSession object
     * @throws Exception
     */
    public abstract Object getInstance(int port, String host,
                                       final RecvCounterCallback clientCallback, String... params)
                                                                                                  throws Exception;
}
