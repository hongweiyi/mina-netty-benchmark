package com.hongweiyi.jmeter;

import com.hongweiyi.jmeter.server.BenchmarkServer;
import com.hongweiyi.jmeter.server.Netty4TcpBenchmarkServer;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.io.IOException;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 8:16 PM
 */
public class Netty4ServerTcpBenchmarkTest extends BenchmarkTest {

    protected static BenchmarkServer server;
    protected static int             port;

    // create a single server to meet the demand of many clients
    static {
        server = new Netty4TcpBenchmarkServer();
        port = BenchmarkServer.getNextAvailable();
        try {
            server.start(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.server = server;
        super.port = port;
        super.setupTest(context);
    }

}
