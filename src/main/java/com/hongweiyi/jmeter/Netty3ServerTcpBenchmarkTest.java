package com.hongweiyi.jmeter;


import com.hongweiyi.jmeter.server.BenchmarkServer;
import com.hongweiyi.jmeter.server.Netty3TcpBenchmarkServer;

import java.io.IOException;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 8:16 PM
 */
public class Netty3ServerTcpBenchmarkTest extends BenchmarkTest {

    // create a single server to meet the demand of many clients
    static {
        server = new Netty3TcpBenchmarkServer();
        port = BenchmarkServer.getNextAvailable();
        try {
            server.start(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
