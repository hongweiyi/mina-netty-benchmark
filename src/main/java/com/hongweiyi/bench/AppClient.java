package com.hongweiyi.bench;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import simperf.Simperf;
import simperf.command.SimperfCommand;
import simperf.thread.SimperfThread;
import simperf.thread.SimperfThreadFactory;

/**
 * @author hongwei.yhw
 * @version 2014-06-02. 3:08 PM
 */
public class AppClient {

    Simperf perf;

    public AppClient(String[] args) throws Exception {
        perf = initSimperf(args);
        if (perf == null) {
            // fail to parse the args
            throw new Exception("fail to parse the args");
        }
    }

    public void start() {
        perf.start();
    }

    /**
     * although simperf has timeout mechanism,
     * netty/mina thread will wait on selector.select() method.
     *
     * the client need to call System.exit explicitly
     */
    public void waitComplete() {
        while (perf.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private Simperf initSimperf(String[] args) {
        final SimperfCommand simCommand = new BenchmarkCommand(args);
        Simperf perf = simCommand.create();
        if (perf == null) {
            return null;
        }

        perf.setThreadFactory(new SimperfThreadFactory() {
            int     port;
            String[] hosts;
            LibType client;
            int     msgSize   = 1024;
            int     warmCount = -1;
            {
                CommandLine cmd = simCommand.getCmd();
                port = Integer.parseInt(cmd.getOptionValue("p"));
                hosts = cmd.getOptionValues("H");
                client = LibType.valueOf(cmd.getOptionValue("C").toUpperCase());
                if (cmd.hasOption("m")) {
                    msgSize = Integer.parseInt(cmd.getOptionValue("m"));
                }
                if (cmd.hasOption("w")) {
                    warmCount = Integer.parseInt(cmd.getOptionValue("w"));
                }
            }

            @Override
            public SimperfThread newThread() {
                return new BenchmarkThread(port, hosts, msgSize, warmCount, client, null);
            }
        });

        return perf;
    }

    /**
     * extends SimperfCommand to reuse options
     */
    public static class BenchmarkCommand extends SimperfCommand {

        public BenchmarkCommand(String[] args) {
            super(args);
            Option port = new Option("p", "port", true, "[*] server port");
            Option hosts = new Option("H", "hosts", true, "[*] server hosts, separate by ,");
            Option client = new Option("C", "client", true,
                "[*] client type. netty3 | netty4 | mina3");
            port.setRequired(true);
            hosts.setRequired(true);
            client.setRequired(true);
            super.getOptions().addOption(port);
            super.getOptions().addOption(hosts);
            super.getOptions().addOption(client);

            super.getOptions().addOption("m", "msgsize", true, "[ ] msg size per request");
            super.getOptions().addOption("w", "warmcount", true, "[ ] request count before start");
        }
    }

    public static void main(String[] args) throws Exception {
        AppClient client = new AppClient(args);
        client.start();

        client.waitComplete();
    }
}
