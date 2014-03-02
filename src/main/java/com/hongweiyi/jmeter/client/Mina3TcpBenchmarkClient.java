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
package com.hongweiyi.jmeter.client;

import com.hongweiyi.jmeter.RecvCounterCallback;
import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoHandler;
import org.apache.mina.api.IoService;
import org.apache.mina.api.IoSession;
import org.apache.mina.transport.nio.NioTcpClient;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 9:16 PM
 */
public class Mina3TcpBenchmarkClient extends BenchmarkClient {

    public static final String LABEL = "Mina3";

    // The TCP client
    private NioTcpClient client;

    @Override
    public Object getClient(int port, final RecvCounterCallback clientCallback, String ... params)
            throws Exception {
        client = new NioTcpClient();
        client.getSessionConfig().setSendBufferSize(64 * 1024);
        client.getSessionConfig().setTcpNoDelay(true);
        client.setIoHandler(new IoHandler() {

            @Override
            public void sessionOpened(IoSession session) {
            }

            @Override
            public void messageReceived(IoSession session, Object message) {
                if (message instanceof ByteBuffer) {
                    clientCallback.receive();
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

        try {
            return client.connect(new InetSocketAddress(port)).get();
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e;
        }
    }

    @Override
    public String getLabel() {
        return LABEL + " -> " + super.getLabel();
    }

}
