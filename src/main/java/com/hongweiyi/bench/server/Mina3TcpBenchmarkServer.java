package com.hongweiyi.bench.server;

import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoHandler;
import org.apache.mina.api.IoService;
import org.apache.mina.api.IoSession;
import org.apache.mina.session.AttributeKey;
import org.apache.mina.transport.nio.FixedSelectorLoopPool;
import org.apache.mina.transport.nio.NioTcpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author hongwei.yhw
 * @version 2014-02-23. 7:52 PM
 */
public class Mina3TcpBenchmarkServer extends BenchmarkServer {

    private static final ByteBuffer               ACK              = ByteBuffer.allocate(1);

    static {
        ACK.put((byte) 0);
        ACK.rewind();
    }

    private static final AttributeKey<ByteBuffer> CUMULATIN_ATTRIBUTE = new AttributeKey<ByteBuffer>(
                                                                       ByteBuffer.class,
                                                                       Mina3TcpBenchmarkServer.class
                                                                           .getName() + ".buf");

    private static final AttributeKey<Integer>    LENGTH_ATTRIBUTE = new AttributeKey<Integer>(
                                                                       Integer.class,
                                                                       Mina3TcpBenchmarkServer.class
                                                                           .getName() + ".length");

    private NioTcpServer                          tcpServer;

    @Override
    public void start(int port) throws IOException {
        tcpServer = new NioTcpServer(new FixedSelectorLoopPool("Server", 1), null);
        tcpServer.getSessionConfig().setReadBufferSize(64 * 1024);
        tcpServer.getSessionConfig().setTcpNoDelay(true);
        tcpServer.setIoHandler(new IoHandler() {
            public void sessionOpened(IoSession session) {
                session.setAttribute(CUMULATIN_ATTRIBUTE, ByteBuffer.allocate(64 * 1024));
                session.setAttribute(LENGTH_ATTRIBUTE, 0);
            }

            public void messageReceived(IoSession session, Object message) {
                if (message instanceof ByteBuffer) {
                    ByteBuffer buffer = (ByteBuffer) message;

                    ByteBuffer cumulation = session.getAttribute(CUMULATIN_ATTRIBUTE);
                    int length = 0;

                    if (session.getAttribute(LENGTH_ATTRIBUTE) != null) {
                        length = session.getAttribute(LENGTH_ATTRIBUTE);
                    }

                    cumulation.put(buffer);
                    cumulation.flip();

                    // I'm too lazy to write a independent DecodeHandler
                    // Aha...
                    while (cumulation.remaining() > 0) {
                        int remaining = cumulation.remaining();
                        if (length == 0) { // has nothing more to read
                            if (remaining >= 4) {
                                length = (cumulation.get() & 255) << 24;
                                length += (cumulation.get() & 255) << 16;
                                length += (cumulation.get() & 255) << 8;
                                length += (cumulation.get() & 255);
                                remaining = cumulation.remaining();
                            } else {
                                break; // remaining data cannot satisfied header length demand
                            }
                        }

                        int readerIndex = cumulation.position();

                        if ((length == 0)) { // only header, no body data
                            session.write(ACK.slice());
                        } else if (length > remaining) { // body length less than expect length
                            cumulation.position(readerIndex + remaining);
                            length -= remaining;
                        } else if (length == remaining) {
                            cumulation.position(readerIndex + remaining);
                            length = 0;
                            session.write(ACK.slice());
                        } else if (length < remaining) {
                            cumulation.position(readerIndex + length);
                            length = 0;
                            session.write(ACK.slice());
                        }
                    }

                    if (cumulation.remaining() > 0) {
                        cumulation.clear();
                    } else {
                        cumulation.compact();
                    }

                    session.setAttribute(CUMULATIN_ATTRIBUTE, cumulation);
                    session.setAttribute(LENGTH_ATTRIBUTE, length);
                }
            }

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

        tcpServer.bind(new InetSocketAddress(port));
    }

    @Override
    public void stop() throws IOException {
    }

}
