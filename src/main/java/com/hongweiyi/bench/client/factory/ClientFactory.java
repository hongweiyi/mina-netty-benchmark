package com.hongweiyi.bench.client.factory;

import com.hongweiyi.bench.LibType;
import com.hongweiyi.bench.RecvCounterCallback;
import com.hongweiyi.bench.client.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hongwei.yhw
 * @version 2014-06-04. 19:03 PM
 */
public class ClientFactory {
    private static Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

    public static Client createClient(String host, int port, int connectionNum, LibType type,
                                      byte[] data, RecvCounterCallback clientCallback)
                                                                                      throws Exception {
        Random random = new Random();
        String key = host + ":" + port + "" + random.nextInt(connectionNum);
        Client client = clients.get(key);
        if (client == null) {
            BenchmarkClient clientInternal = getClientInternal(type);
            synchronized (ClientFactory.class) {
                if (client == null) {
                    client = ClientFactory.createClient(type, data,
                        clientInternal.getInstance(port, host, clientCallback));
                }
            }
            clients.put(key, client);
        }

        return client;
    }

    private static Client createClient(LibType type, byte[] data, Object client) {
        Client clientProxy;
        if (LibType.MINA3.equals(type)) {
            clientProxy = new Mina3Client(data, client);
        } else if (LibType.NETTY3.equals(type)) {
            clientProxy = new Netty3Client(data, client);
        } else if (LibType.NETTY4.equals(type)) {
            clientProxy = new Netty4Client(data, client);
        } else {
            throw new RuntimeException("No such client type: " + type);
        }

        return clientProxy;
    }

    private static BenchmarkClient getClientInternal(LibType clientType) throws Exception {
        BenchmarkClient bcmClient = null;
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

}
