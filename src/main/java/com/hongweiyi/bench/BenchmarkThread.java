package com.hongweiyi.bench;

import com.hongweiyi.bench.client.Client;
import com.hongweiyi.bench.client.factory.ClientFactory;
import simperf.thread.SimperfThread;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 5:22 PM
 */
public class BenchmarkThread extends SimperfThread {

    private Client<Integer> client;
    private int             warmCount = 0;

    public BenchmarkThread(int port, String[] hosts, int messageSize, int warmCount,
                           int connectionNum, LibType clientType, String paramAlloc) {
        // init send data
        byte[] data = new byte[messageSize + 4];
        data[0] = (byte) (messageSize >>> 24 & 255);
        data[1] = (byte) (messageSize >>> 16 & 255);
        data[2] = (byte) (messageSize >>> 8 & 255);
        data[3] = (byte) (messageSize & 255);

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
        client.send();
        try { // async convert to sync
            // get any response from server
            // whether the response is return for this client.send() or not
            Integer num = client.poll(1, TimeUnit.SECONDS);
            if (num == null) {
                throw new InterruptedException("timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
