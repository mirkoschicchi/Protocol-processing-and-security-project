package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.scenarios.LanScenarioTest;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ServerClientTest extends LanScenarioTest {
    @Test
    public void listenServer() {
        var tcpServer = Server.listen(server.getInterface(), (short) 80, TestConnection.class);

        assertNotNull(tcpServer);
    }

    @Test
    public void openConnection() {
        var tcpServer = Server.listen(server.getInterface(), (short) 80, TestConnection.class);
        var connectedEvent = new CountDownLatch(1);

        var clientConnection = new Connection(client.getInterface()) {
            @Override
            public void connected(DatagramHandler.ConnectionState connectionState) {
                super.connected(connectionState);
                connectedEvent.countDown();
            }
        };

        assertNull(TestConnection.lastConnection);

        clientConnection.connect(server.getIpAddress(), (short) 80);

        try {
            assertTrue(connectedEvent.await(1, TimeUnit.SECONDS));
            assertNotNull(TestConnection.lastConnection);
            assertEquals(1, TestConnection.lastConnection.state);
        } catch (InterruptedException e) {
            fail(e);
        }

        clientConnection.close();
        tcpServer.shutdown();
    }

    @Test
    public void sendMessage() {
        var tcpServer = Server.listen(server.getInterface(), (short) 80, TestConnection.class);
        var connectedEvent = new CountDownLatch(1);

        var clientConnection = new Connection(client.getInterface()) {
            @Override
            public void connected(DatagramHandler.ConnectionState connectionState) {
                super.connected(connectionState);

                connectedEvent.countDown();
                send("Test".getBytes());
            }
        };

        assertNull(TestConnection.lastConnection);

        clientConnection.connect(server.getIpAddress(), (short) 80);

        try {
            assertTrue(connectedEvent.await(1, TimeUnit.SECONDS));
            assertNotNull(TestConnection.lastConnection);
            assertEquals(2, TestConnection.lastConnection.state);
        } catch (InterruptedException e) {
            fail(e);
        }

        clientConnection.close();
        tcpServer.shutdown();
    }

    @Test
    public void sendReply() {
        var tcpServer = Server.listen(server.getInterface(), (short) 80, TestConnection.class);

        var repliedEvent = new CountDownLatch(1);

        var clientConnection = new Connection(client.getInterface()) {
            @Override
            public void connected(DatagramHandler.ConnectionState connectionState) {
                super.connected(connectionState);
                send("Test".getBytes());
            }

            @Override
            public void messageReceived(byte[] message) {
                assertArrayEquals("Reply".getBytes(), message);
                repliedEvent.countDown();
            }
        };

        assertNull(TestConnection.lastConnection);
        TestConnection.nextReply = "Reply".getBytes();

        clientConnection.connect(server.getIpAddress(), (short) 80);

        try {
            assertTrue(repliedEvent.await(1, TimeUnit.SECONDS));
            assertNotNull(TestConnection.lastConnection);
            assertEquals(2, TestConnection.lastConnection.state);
        } catch (InterruptedException e) {
            fail(e);
        }

        clientConnection.close();
        tcpServer.shutdown();
    }

    @Test
    public void closeConnection() {
        var tcpServer = Server.listen(server.getInterface(), (short) 80, TestConnection.class);
        var connectedEvent = new CountDownLatch(1);
        var disconnectedEvent = new CountDownLatch(1);

        var clientConnection = new Connection(client.getInterface()) {
            @Override
            public void connected(DatagramHandler.ConnectionState connectionState) {
                super.connected(connectionState);

                connectedEvent.countDown();
            }

            @Override
            public void closed() {
                super.closed();

                disconnectedEvent.countDown();
            }
        };

        assertNull(TestConnection.lastConnection);

        clientConnection.connect(server.getIpAddress(), (short) 80);

        try {
            assertTrue(connectedEvent.await(1, TimeUnit.SECONDS));
            assertNotNull(TestConnection.lastConnection);
            assertEquals(1, TestConnection.lastConnection.state);

            clientConnection.close();

            assertTrue(disconnectedEvent.await(1, TimeUnit.SECONDS));
            assertEquals(0, TestConnection.lastConnection.state);
        } catch (InterruptedException e) {
            fail(e);
        }

        tcpServer.shutdown();
    }

    @BeforeEach
    public void clearConnection() {
        TestConnection.lastConnection = null;
        TestConnection.nextReply = null;
    }

    static class TestConnection extends Connection {
        private static TestConnection lastConnection;
        private static byte[] nextReply;
        public int state;
        public byte[] lastMessage;

        public TestConnection(EthernetInterface ethernetInterface) {
            super(ethernetInterface);

            lastConnection = this;
        }

        @Override
        public void connected(DatagramHandler.ConnectionState connectionState) {
            super.connected(connectionState);

            state = 1;
        }

        @Override
        public void messageReceived(byte[] message) {
            state = 2;
            lastMessage = message;
            if (nextReply != null) {
                send(nextReply);
            }
        }

        @Override
        public void closed() {
            super.closed();

            state = 0;
        }
    }
}
