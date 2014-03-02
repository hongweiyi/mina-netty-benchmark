package com.hongweiyi.jmeter.server;


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

    public final static String LABEL = "Mina3-Server";

    private static enum State {
        WAIT_FOR_FIRST_BYTE_LENGTH, WAIT_FOR_SECOND_BYTE_LENGTH, WAIT_FOR_THIRD_BYTE_LENGTH, WAIT_FOR_FOURTH_BYTE_LENGTH, READING
    }

    private static final ByteBuffer ACK = ByteBuffer.allocate(1);

    static {
        ACK.put((byte) 0);
        ACK.rewind();
    }

    private static final AttributeKey<State> STATE_ATTRIBUTE = new AttributeKey<State>(State.class,
            Mina3TcpBenchmarkServer.class.getName() + ".state");

    private static final AttributeKey<Integer> LENGTH_ATTRIBUTE = new AttributeKey<Integer>(Integer.class,
            Mina3TcpBenchmarkServer.class.getName() + ".length");

    private NioTcpServer tcpServer;


    @Override
    public void start(int port) throws IOException {
        tcpServer = new NioTcpServer(new FixedSelectorLoopPool("Server", 1), null);
        tcpServer.getSessionConfig().setReadBufferSize(128 * 1024);
        tcpServer.getSessionConfig().setTcpNoDelay(true);
        tcpServer.setIoHandler(new IoHandler() {
            public void sessionOpened(IoSession session) {
                session.setAttribute(STATE_ATTRIBUTE, State.WAIT_FOR_FIRST_BYTE_LENGTH);
            }

            public void messageReceived(IoSession session, Object message) {
                if (message instanceof ByteBuffer) {
                    ByteBuffer buffer = (ByteBuffer) message;

                    State state = session.getAttribute(STATE_ATTRIBUTE);
                    int length = 0;

                    if (session.getAttribute(LENGTH_ATTRIBUTE) != null) {
                        length = session.getAttribute(LENGTH_ATTRIBUTE);
                    }

                    while (buffer.remaining() > 0) {
                        switch (state) {
                            case WAIT_FOR_FIRST_BYTE_LENGTH:
                                length = (buffer.get() & 255) << 24;
                                state = State.WAIT_FOR_SECOND_BYTE_LENGTH;
                                break;
                            case WAIT_FOR_SECOND_BYTE_LENGTH:
                                length += (buffer.get() & 255) << 16;
                                state = State.WAIT_FOR_THIRD_BYTE_LENGTH;
                                break;
                            case WAIT_FOR_THIRD_BYTE_LENGTH:
                                length += (buffer.get() & 255) << 8;
                                state = State.WAIT_FOR_FOURTH_BYTE_LENGTH;
                                break;
                            case WAIT_FOR_FOURTH_BYTE_LENGTH:
                                length += (buffer.get() & 255);
                                state = State.READING;
                                if ((length == 0) && (buffer.remaining() == 0)) {
                                    session.write(ACK.slice());
                                    state = State.WAIT_FOR_FIRST_BYTE_LENGTH;
                                }
                                break;
                            case READING:
                                int remaining = buffer.remaining();
                                if (length > remaining) {
                                    length -= remaining;
                                    buffer.position(buffer.position() + remaining);
                                } else {
                                    buffer.position(buffer.position() + length);
                                    session.write(ACK.slice());
                                    state = State.WAIT_FOR_FIRST_BYTE_LENGTH;
                                    length = 0;
                                }
                        }
                    }
                    session.setAttribute(LENGTH_ATTRIBUTE, length);
                    session.setAttribute(STATE_ATTRIBUTE, state);
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
        // do nothing
        // Can not close the server
        // Server is a static object
        // so Server will last until class is unloaded
    }

    @Override
    public String getLabel() {
       return LABEL;
    }
}
