package com.hongweiyi.jmeter;


import com.hongweiyi.jmeter.server.BenchmarkServer;
import com.hongweiyi.jmeter.server.Mina3TcpBenchmarkServer;

import java.io.IOException;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 8:16 PM
 */
public class Mina3ServerTcpBenchmarkTest extends BenchmarkTest {

    // create a single server to meet the demand of many clients
    static {
        server = new Mina3TcpBenchmarkServer();
        port = BenchmarkServer.getNextAvailable();
        try {
            server.start(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
