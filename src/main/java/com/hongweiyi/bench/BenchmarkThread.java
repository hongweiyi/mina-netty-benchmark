package com.hongweiyi.bench;

import com.hongweiyi.bench.client.Client;
import com.hongweiyi.bench.client.factory.ClientFactory;
import simperf.thread.SimperfThread;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 5:22 PM
 */
public class BenchmarkThread extends SimperfThread {

    private Client<Integer>   client;
    private int               warmCount = 0;
    private static AtomicLong GLOBAL_ID = new AtomicLong(0);

    public BenchmarkThread(int port, String[] hosts, int messageSize, int warmCount,
                           int connectionNum, LibType clientType, String paramAlloc) {
        // init send data
        byte[] data = new byte[messageSize];

        this.warmCount = warmCount;

        String host = "localhost";
        if (null != hosts && hosts.length > 0) {
            Random random = new Random();
            host = hosts[random.nextInt(hosts.length)];
        }

        // init client
        try {
            client = ClientFactory.createClient(host, port, connectionNum, clientType, data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean runTask() {
        return invokeSync();
    }

    @Override
    public void warmUp() {
        while (warmCount-- > 0) {
            runTask();
        }
    }

    @Override
    public void afterRunTask() {
        client.close();
    }

    /**
     * async convert to sync
     *
     * @return is invoked success
     */
    private boolean invokeSync() {
        long id = GLOBAL_ID.getAndDecrement();
        client.send(id);
        try { // async convert to sync
              // get any response from server
              // whether the response is return for this client.send() or not
            Integer num = client.poll(id, 3, TimeUnit.SECONDS);
            if (num == null) {
                throw new Exception("timeout: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
