package com.hongweiyi.bench;

import com.hongweiyi.bench.client.*;
import simperf.thread.SimperfThread;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 5:22 PM
 */
public class BenchmarkThread extends SimperfThread {

    protected CountDownLatch      recvCounter;
    protected RecvCounterCallback clientCallback;

    private Client                client;
    private LibType               type;

    private int                   warmCount = 0;

    private Random random  = new Random();

    public BenchmarkClient getClientInternal(LibType clientType) throws Exception {
        BenchmarkClient bcmClient = null;
        type = clientType;
        if (clientType.equals(LibType.NETTY3)) {
            bcmClient = new Netty3TcpBenchmarkClient();
        } else if (clientType.equals(LibType.NETTY4)) {
            bcmClient = new Netty4TcpBenchmarkClient();
        } else if (clientType.equals(LibType.MINA3)) {
            bcmClient = new Mina3TcpBenchmarkClient();
        }

        if (null != bcmClient) {
            return bcmClient;
        } else {
            throw new Exception("client_type must be netty3 | netty4 | mina3 !");
        }
    }

    public BenchmarkThread(int port, String[] hosts, int messageSize, int warmCount,
                           LibType clientType, String paramAlloc) {
        // init send data
        byte[] data = new byte[messageSize + 4];
        data[0] = (byte) (messageSize >>> 24 & 255);
        data[1] = (byte) (messageSize >>> 16 & 255);
        data[2] = (byte) (messageSize >>> 8 & 255);
        data[3] = (byte) (messageSize & 255);

        this.warmCount = warmCount;

        String host = "localhost";
        if (null != hosts && hosts.length > 0) {
            host = hosts[random.nextInt(hosts.length)];
        }

        // init client
        try {
            recvCounter = new CountDownLatch(1);

            clientCallback = new RecvCounterCallback() {
                @Override
                public void receive() {
                    recvCounter.countDown();
                }
            };
            BenchmarkClient clientInternal = getClientInternal(clientType);
            if (LibType.MINA3.equals(type)) {
                client = new Mina3Client(data,
                    clientInternal.getClient(port, host, clientCallback));
            } else if (LibType.NETTY3.equals(type)) {
                client = new Netty3Client(data, clientInternal.getClient(port, host,
                    clientCallback));
            } else if (LibType.NETTY4.equals(type)) {
                client = new Netty4Client(data, clientInternal.getClient(port, host,
                    clientCallback, paramAlloc));
            } else {
                throw new RuntimeException("No such client type: " + type);
            }

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
            recvCounter.await(1, TimeUnit.SECONDS);
            if (recvCounter.getCount() == 1) {
                throw new InterruptedException("recvCounter await out of time");
            }
            recvCounter = new CountDownLatch(1); // ignore the cost of initing a new CountDownLatch
        } catch (InterruptedException e) {
            e.printStackTrace();
            recvCounter = new CountDownLatch(1);
            return false;
        }

        return true;
    }

}
