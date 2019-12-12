package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class DatagramHandler {
    private static final Logger LOGGER = Logger.getLogger("DatagramHandler");
    private final Map<ConnectionDescriptor, ConnectionState> stateTable = Collections.synchronizedMap(new HashMap<>());
    private final Map<Short, Server> servers = Collections.synchronizedMap(new HashMap<>());
    private final NetworkNode node;
    private final Consumer<IPv6Packet> packetSender;
    private Disposable listener;

    public DatagramHandler(NetworkNode node, Consumer<IPv6Packet> packetSender) {
        this.packetSender = packetSender;
        Objects.requireNonNull(node);

        this.node = node;
    }

    public void start() {
        if (listener == null) {
            listener = Flux.merge((Iterable<Flux<byte[]>>)
                    node.getInterfaces().stream()
                            .map(i -> i.getFlux().doOnEach(pdu -> onMessage(i, pdu.get())))::iterator
            ).subscribe();
        }
    }

    public void connect(Connection connection, IPAddress ipAddress, short port) {
        Objects.requireNonNull(connection);
        Objects.requireNonNull(ipAddress);

        var route = node.getRoutingTable().getRowByDestinationAddress(ipAddress, null);
        if (route == null) {
            throw new UnsupportedOperationException("Could not determine route for " + ipAddress);
        }

        ConnectionState state = null;
        var valid = false;
        var rnd = new Random();
        for (var i = 0; i < 100; i++) {
            var srcPort = (short) (rnd.nextLong() & 0x7fff | 0x8000);
            var descriptor = new ConnectionDescriptor(route.getInterface().getIpAddress(), ipAddress, srcPort, port);
            state = new ConnectionState(descriptor, connection);
            if (stateTable.putIfAbsent(descriptor, state) == null) {
                // YAY, port is free
                valid = true;
                break;
            }
        }

        if (!valid) {
            throw new IllegalStateException("Could not find available client port.");
        }

        state.send(null, TCPDatagram.SYN);
    }

    public void listen(short port, Server server) {
        servers.put(port, server);
    }

    private void onMessage(EthernetInterface intf, byte[] pdu) {
        var frame = EthernetFrame.parse(pdu);
        if (Arrays.equals(frame.getDestination(), intf.getAddress()) && frame.getType() == EthernetFrame.TYPE_IPV6) {
            var packet = IPv6Packet.parse(frame.getPayload());
            if (intf.getIpAddress().equals(packet.getDestinationIP()) && packet.getNextHeader() == 0x6) {
                var datagram = TCPDatagram.parse(packet.getPayload());
                var descriptor = new ConnectionDescriptor(
                        packet.getDestinationIP(), packet.getSourceIP(),
                        datagram.getDestinationPort(), datagram.getSourcePort()
                );

                ConnectionState state = stateTable.get(descriptor);
                if (state != null) {
                    state.update(datagram);
                }

                short flags = datagram.getFlags();

                if (state == null) {
                    if (flags == TCPDatagram.SYN) {
                        // Set up new connection to server
                        var server = servers.get(datagram.getDestinationPort());
                        if (server != null) {
                            var connection = server.accept(descriptor);
                            if (connection != null) {
                                state = new ConnectionState(descriptor, connection);
                                state.update(datagram);

                                stateTable.put(descriptor, state);

                                state.send(null, (short) (TCPDatagram.SYN | TCPDatagram.ACK));
                            }
                        }
                    }
                } else if (flags == (TCPDatagram.SYN | TCPDatagram.ACK) && state.status == ConnectionStatus.Setup) {
                    if (datagram.getAckN() == state.seqN) {
                        // Connection is establishing
                        state.send(null, TCPDatagram.ACK);
                        state.status = ConnectionStatus.Established;
                        state.connection.connected(state);
                    } else {
                        state.connection.closed();
                        stateTable.remove(state.descriptor);
                    }
                } else if ((flags & TCPDatagram.FIN) == TCPDatagram.FIN) {
                    switch (state.status) {
                        case Established:
                            state.status = ConnectionStatus.Closing;
                            state.send(null, (short) (TCPDatagram.FIN | TCPDatagram.ACK));
                            break;
                        case Closing:
                            state.status = ConnectionStatus.Closed;
                            state.connection.closed();
                            state.send(null, TCPDatagram.ACK);
                            stateTable.remove(state.descriptor);
                            break;
                    }
                } else if (flags == TCPDatagram.RST) {
                    state.status = ConnectionStatus.Closed;
                    state.connection.closed();
                } else if (state.status == ConnectionStatus.Established) {
                    if (datagram.getPayload() != null && datagram.getPayload().length > 0) {
                        state.connection.messageReceived(datagram.getPayload());
                    }
                } else if (flags == TCPDatagram.ACK) {
                    switch (state.status) {
                        case Setup:
                            state.status = ConnectionStatus.Established;
                            state.connection.connected(state);
                            break;
                        case Closing:
                            state.connection.closed();
                            stateTable.remove(state.descriptor);
                            break;
                    }
                }
            }
        }
    }

    public void close(Server server) {
        for (var s : new HashSet<>(servers.entrySet())) {
            if (s.getValue() == server) {
                servers.remove(s.getKey());
            }
        }
    }

    public void stop() {
        if (listener != null) {
            listener.dispose();
            listener = null;
        }
    }

    public static class ConnectionDescriptor {
        private final IPAddress localIp, remoteIp;
        private final short localPort, remotePort;

        ConnectionDescriptor(IPAddress localIp, IPAddress remoteIp, short localPort, short remotePort) {
            this.localIp = localIp;
            this.remoteIp = remoteIp;
            this.localPort = localPort;
            this.remotePort = remotePort;
        }

        @Override
        public int hashCode() {
            return Objects.hash(localIp, remoteIp, localPort, remotePort);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ConnectionDescriptor) {
                var other = (ConnectionDescriptor) obj;
                return other.localPort == localPort
                        && other.remotePort == remotePort
                        && other.localIp.equals(localIp)
                        && other.remoteIp.equals(remoteIp);
            }

            return super.equals(obj);
        }

        @Override
        public String toString() {
            return "[" + localIp + "]:" + (localPort & 0xffff) + " -> [" + remoteIp + "]:" + (remotePort & 0xffff);
        }

        public IPAddress getLocalIp() {
            return localIp;
        }

        public IPAddress getRemoteIp() {
            return remoteIp;
        }

        public short getLocalPort() {
            return localPort;
        }

        public short getRemotePort() {
            return remotePort;
        }
    }

    public enum ConnectionStatus {
        Closed,
        Setup,
        Established,
        Closing,
    }

    public class ConnectionState {
        private final ConnectionDescriptor descriptor;
        private final Connection connection;
        private ConnectionStatus status;
        private int seqN;
        private int ackN;

        ConnectionState(ConnectionDescriptor descriptor, Connection connection) {
            Objects.requireNonNull(descriptor);
            Objects.requireNonNull(connection);

            this.descriptor = descriptor;
            this.connection = connection;
            this.status = ConnectionStatus.Setup;

            var rnd = new Random();
            this.seqN = rnd.nextInt();
        }

        void update(TCPDatagram datagram) {
            ackN = datagram.getSeqN();

            if (datagram.getPayload() == null || datagram.getPayload().length == 0) {
                ackN++;
            } else {
                ackN += datagram.getPayload().length;
            }
        }

        public void send(byte[] message) {
            send(message, (short) 0);
        }

        void send(byte[] message, short flags) {
            if ((flags & (TCPDatagram.SYN | TCPDatagram.RST)) == 0) {
                flags |= TCPDatagram.ACK;
            }

            var ackN = (flags & (TCPDatagram.ACK | TCPDatagram.SYN)) != 0 ? this.ackN : 0;
            var datagram = TCPDatagram.create(descriptor.localPort, descriptor.remotePort, this.seqN, ackN, flags,
                    (short) 0xffff, message);

            if (message != null && message.length > 0) {
                seqN += message.length;
            } else if (flags != TCPDatagram.ACK) {
                seqN++;
            }

            var packet = IPv6Packet.create((byte) 0x6, descriptor.localIp, descriptor.remoteIp, datagram.serialize(descriptor.localIp, descriptor.remoteIp));

            packetSender.accept(packet);
        }

        public void close() {
            status = ConnectionStatus.Closing;
            send(null, TCPDatagram.FIN);
        }

        public ConnectionDescriptor getDescriptor() {
            return descriptor;
        }

        public ConnectionStatus getStatus() {
            return status;
        }
    }
}
