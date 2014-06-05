package com.hongweiyi.bench.client.factory;

import com.hongweiyi.bench.LibType;
import com.hongweiyi.bench.client.Client;
import com.hongweiyi.bench.client.Mina3Client;
import com.hongweiyi.bench.client.Netty3Client;
import com.hongweiyi.bench.client.Netty4Client;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hongwei.yhw
 * @version 2014-06-04. 19:03 PM
 */
public class ClientFactory {
    private static Map<String, Client<Integer>> clients = new ConcurrentHashMap<String, Client<Integer>>();

    public static Client<Integer> createClient(String host, int port, int connectionNum, LibType type,
                                      byte[] data) throws Exception {
        Random random = new Random();
        String key = host + ":" + port + "" + random.nextInt(connectionNum);
        Client<Integer> client = clients.get(key);
        if (client == null) {
            synchronized (ClientFactory.class) {
                if (client == null) {
                    client = ClientFactory.createClient(type, data);
                    client.init(host, port, 1);
                }
            }
            clients.put(key, client);
        }

        return client;
    }

    private static Client<Integer> createClient(LibType type, byte[] data) {
        Client<Integer> clientProxy;
        if (LibType.MINA3.equals(type)) {
            clientProxy = new Mina3Client<Integer>(data);
        } else if (LibType.NETTY3.equals(type)) {
            clientProxy = new Netty3Client<Integer>(data);
        } else if (LibType.NETTY4.equals(type)) {
            clientProxy = new Netty4Client<Integer>(data);
        } else {
            throw new RuntimeException("No such client type: " + type);
        }

        return clientProxy;
    }

}
