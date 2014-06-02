package com.hongweiyi.bench.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.NoSuchElementException;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 7:52 PM
 */
public abstract class BenchmarkServer {

    public abstract void start(int port) throws IOException;

    public abstract void stop() throws IOException;

    /**
     * gen a available port for benchmark server
     *
     * @return socket port
     */
    public static int getNextAvailable() {
        ServerSocket serverSocket;

        try {
            // Here, we simply return an available port found by the system
            serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();

            // Don't forget to close the socket...
            serverSocket.close();

            return port;
        } catch (IOException ioe) {
            throw new NoSuchElementException(ioe.getMessage());
        }
    }

}
