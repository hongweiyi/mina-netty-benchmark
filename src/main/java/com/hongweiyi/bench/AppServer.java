package com.hongweiyi.bench;

import com.hongweiyi.bench.server.BenchmarkServer;
import com.hongweiyi.bench.server.Mina3TcpBenchmarkServer;
import com.hongweiyi.bench.server.Netty3TcpBenchmarkServer;
import com.hongweiyi.bench.server.Netty4TcpBenchmarkServer;
import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * @author hongwei.yhw
 * @version 2014-06-02. 3:08 PM
 */
public class AppServer {
    private Options options = new Options();

    int             port    = 8000;
    LibType         serverType;

    public AppServer(String[] args) {
        Option port = new Option("p", "port", true, "[ ] server port");
        Option server = new Option("s", "server", true, "[ ] server type. netty3 | netty4 | mina3");
        port.setRequired(true);
        server.setRequired(true);
        options.addOption(port);
        options.addOption(server);
        try {
            CommandLine cmd = new PosixParser().parse(options, args);
            this.port = Integer.parseInt(cmd.getOptionValue("p"));
            this.serverType = LibType.valueOf(cmd.getOptionValue("s").toUpperCase());
        } catch (ParseException e) {
            new HelpFormatter().printHelp("SimperfCommand options", options);
        }
    }

    public void start() {
        BenchmarkServer server;
        if (serverType.equals(LibType.MINA3)) {
            server = new Mina3TcpBenchmarkServer();
        } else if (serverType.equals(LibType.NETTY3)) {
            server = new Netty3TcpBenchmarkServer();
        } else if (serverType.equals(LibType.NETTY4)) {
            server = new Netty4TcpBenchmarkServer();
        } else {
            server = null;
        }

        if (server != null) {
            try {
                server.start(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("No such server type: " + serverType);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AppServer server = new AppServer(args);
        server.start();
    }
}
