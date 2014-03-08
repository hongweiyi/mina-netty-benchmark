package com.hongweiyi.jmeter;

import com.hongweiyi.jmeter.client.*;
import com.hongweiyi.jmeter.server.BenchmarkServer;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.IOException;
import java.util.concurrent.*;


/**
 * @author hongwei.yhw
 * @version 2014-02-23. 5:22 PM
 */
public abstract class BenchmarkTest extends AbstractJavaSamplerClient {

    public static final String PARAM_NO_OF_MSG = "nubmer_of_msgs";
    public static final String PARAM_SIZE_OF_MSG = "size_of_single_msg";
    public static final String PARAM_NETTY4_ALLOC = "netty4_alloc";
    public static final String PARAM_CLIENT_TYPE = "client_type";

    public static final String PARAM_SEND_ASYNC = "send_aysnc";

    protected CountDownLatch recvCounter;
    protected CountDownLatch sendCounter;
    protected BlockingQueue<byte[]> recvQueue = new LinkedBlockingQueue<byte[]>();

    protected RecvCounterCallback clientCallback;

    private String label;
    private Client client;
    private Client.CLIENT_TYPE type;
    private BenchmarkClient clientInternal;

    private boolean sendAsync;

    private ExecutorService executor = Executors.newFixedThreadPool(16);

    public BenchmarkClient getClientInternal(String clientType) throws Exception{
        BenchmarkClient bcmClient = null;
        if ("netty3".equals(clientType)) {
            type = Client.CLIENT_TYPE.NETTY3;
            bcmClient = new Netty3TcpBenchmarkClient();
        } else if ("netty4".equals(clientType)) {
            type = Client.CLIENT_TYPE.NETTY4;
            bcmClient = new Netty4TcpBenchmarkClient();
        } else if ("mina3".equals(clientType)) {
            type = Client.CLIENT_TYPE.MINA3;
            bcmClient = new Mina3TcpBenchmarkClient();
        }

        if (null != bcmClient) {
            label = bcmClient.getLabel() + " " + server.getLabel();
            return bcmClient;
        } else {
            throw new Exception("client_type must be netty3 | netty4 | mina3 !");
        }
    }


    protected static BenchmarkServer server;
    protected static int port;

    @Override
    public void setupTest(JavaSamplerContext context) {

        recvCounter = new CountDownLatch(1);
        sendCounter = new CountDownLatch(context.getIntParameter(PARAM_NO_OF_MSG) - 1); // -1 just for last request to close channel
        int messageSize = context.getIntParameter(PARAM_SIZE_OF_MSG);
        String paramAlloc = context.getParameter(PARAM_NETTY4_ALLOC);
        String clientType = context.getParameter(PARAM_CLIENT_TYPE);
        String sendAsyncStr = context.getParameter(PARAM_SEND_ASYNC);

        if (Boolean.parseBoolean(sendAsyncStr.trim())) {
            sendAsync = true;
        }

        clientCallback = new RecvCounterCallback() {
            @Override
            public void receive() {
                if (sendAsync) {
                    finishQueue.add(QUEUE_DATA_SUCCESS);
                } else {
                    recvCounter.countDown();
                }
            }
        };

        // init send data
        byte[] data = new byte[messageSize + 4];
        data[0] = (byte) (messageSize >>> 24 & 255);
        data[1] = (byte) (messageSize >>> 16 & 255);
        data[2] = (byte) (messageSize >>> 8 & 255);
        data[3] = (byte) (messageSize & 255);

        // init client
        try {
            clientInternal = getClientInternal(clientType);
            client = new Client(type, data, clientInternal.getClient(port, clientCallback, paramAlloc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult sr = new SampleResult();
        sr.setSampleLabel(label);

        sr.sampleStart();

        boolean isSuccess = false;
        if (sendCounter.getCount() > 0) {
            if (sendAsync) {
                isSuccess = invokeAsync();
            } else {
                isSuccess = invokeSync();
            }
        } else {
            client.close();
            System.out.println("Client "+ ": closed!");
        }

        sr.setSuccessful(isSuccess);
        sr.sampleEnd();
        return sr;
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
        } finally {
            sendCounter.countDown();
        }

        return true;
    }


    private boolean asyncStarted = false;

    /**
     * invoke just once logically
     */
    private void invokeAsyncOnce() {
        if (asyncStarted) {
            return;
        }

        asyncStarted = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int sendCnt = (int)sendCounter.getCount();
                int step = 16;
                while (sendCnt > 0) {
                    if (sendCnt - step < 0) {
                        step = sendCnt;
                    }
                    executor.execute(new Worker(step));
                    sendCnt -= step;
                }
            }
        });
        t.start();
    }

    private boolean invokeAsync() {
        invokeAsyncOnce(); // invoke once, for starting client invoke thread

        boolean isSuccess;
        try {
            Object finishData = finishQueue.poll(30, TimeUnit.SECONDS);
            if (null == finishData) {
                throw new InterruptedException("finishQueue await out of time");
            }
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }

        sendCounter.countDown();
        return isSuccess;

    }

    @Override
    public void teardownTest(JavaSamplerContext arg0) {
        try {
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument(PARAM_NO_OF_MSG, "100000");
        params.addArgument(PARAM_SIZE_OF_MSG, 1024 * 1024 + "");
        params.addArgument(PARAM_NETTY4_ALLOC, "unpooled");
        params.addArgument(PARAM_CLIENT_TYPE, "netty4");
        params.addArgument(PARAM_SEND_ASYNC, "false");
        return params;
    }

    public BlockingQueue<Object> finishQueue = new LinkedBlockingQueue<Object>();
    private final byte[] QUEUE_DATA_SUCCESS = new byte[0];

    private class Worker implements Runnable {

        private int n;

        public Worker(int n) {
            this.n = n;
        }

        @Override
        public void run() {
            while (n-- > 0) {
                client.send();
            }
        }
    }
}
