/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package com.hongweiyi.bench.client;

import com.hongweiyi.bench.RecvCounterCallback;
import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoHandler;
import org.apache.mina.api.IoService;
import org.apache.mina.api.IoSession;
import org.apache.mina.session.AttributeKey;
import org.apache.mina.transport.nio.NioTcpClient;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:16 PM
 */
public class Mina3TcpBenchmarkClient extends BenchmarkClient {

    private static final AttributeKey<ByteBuffer> CUMULATION_ATTRIBUTE = new AttributeKey<ByteBuffer>(
                                                                           ByteBuffer.class,
                                                                           Mina3TcpBenchmarkClient.class
                                                                               .getName()
                                                                                   + ".buffer");

    @Override
    public Object getInstance(String host, int port, final RecvCounterCallback clientCallback,
                              String... params) throws Exception {
        NioTcpClient client = new NioTcpClient();
        client.getSessionConfig().setSendBufferSize(64 * 1024);
        client.getSessionConfig().setTcpNoDelay(true);
        client.setIoHandler(new IoHandler() {

            @Override
            public void sessionOpened(IoSession session) {
            }

            @Override
            public void messageReceived(IoSession session, Object message) {
                if (message instanceof ByteBuffer) {
                    ByteBuffer buffer = (ByteBuffer) message;
                    ByteBuffer cumulation = session.getAttribute(CUMULATION_ATTRIBUTE);
                    if (cumulation == null) {
                        cumulation = ByteBuffer.allocate(64 * 1024 * 10);
                    }

                    cumulation.put(buffer);
                    cumulation.flip();

                    while (cumulation.remaining() >= 8) {
                        long id = cumulation.getLong();
                        clientCallback.receive(id);
                    }

                    if (cumulation.remaining() == 0) {
                        cumulation.clear();
                    } else {
                        cumulation.compact();
                    }
                    session.setAttribute(CUMULATION_ATTRIBUTE, cumulation);
                } else {
                    throw new IllegalArgumentException(message.getClass().getName());
                }
            }

            @Override
            public void exceptionCaught(IoSession session, Exception cause) {
                cause.printStackTrace();
            }

            @Override
            public void sessionClosed(IoSession session) {
            }

            @Override
            public void sessionIdle(IoSession session, IdleStatus status) {
            }

            @Override
            public void messageSent(IoSession session, Object message) {
            }

            @Override
            public void serviceActivated(IoService service) {
            }

            @Override
            public void serviceInactivated(IoService service) {
            }
        });

        return client.connect(new InetSocketAddress(host, port)).get();
    }

}
