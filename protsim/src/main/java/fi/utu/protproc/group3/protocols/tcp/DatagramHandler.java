package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.Disposable;

import java.util.*;

public class DatagramHandler {
    private final Map<ConnectionDescriptor, ConnectionState> stateTable = new HashMap<>();
    private final Map<Short, Server> servers = new HashMap<>();
    private final EthernetInterface ethernetInterface;
    private Disposable listener;

    public DatagramHandler(EthernetInterface ethernetInterface) {
        Objects.requireNonNull(ethernetInterface);

        this.ethernetInterface = ethernetInterface;
    }

    public void start() {
        if (listener == null) {
            listener = ethernetInterface.getFlux().subscribe(this::onMessage);
        }
    }

    public void connect(Connection connection, IPAddress ipAddress, short port) {
        Objects.requireNonNull(connection);
        Objects.requireNonNull(ipAddress);

        ConnectionState state = null;
        var valid = false;
        for (var i = 32268; i < 65535; i++) {
            var descriptor = new ConnectionDescriptor(ethernetInterface.getIpAddress(), ipAddress, (short) i, port);
            state = new ConnectionState(descriptor, this, connection);
            if (stateTable.putIfAbsent(descriptor, state) == null) {
                // YAY, port is free
                valid = true;
                break;
            }
        }

        if (!valid) {
            throw new IllegalStateException("Could not find available client port.");
        }

        assert state != null;

        state.send(null, TCPDatagram.SYN);
    }

    public void listen(short port, Server server) {
        servers.put(port, server);
    }

    public void onMessage(byte[] pdu) {
        var frame = EthernetFrame.parse(pdu);
        if (frame.getType() == EthernetFrame.TYPE_IPV6) {
            var packet = IPv6Packet.parse(frame.getPayload());
            if (packet.getNextHeader() == 0x6) {
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

                            state = new ConnectionState(descriptor, this, connection);
                            state.update(datagram);

                            stateTable.put(descriptor, state);

                            state.send(null, (short) (TCPDatagram.SYN | TCPDatagram.ACK));
                        }
                    }
                } else if (flags == (TCPDatagram.SYN | TCPDatagram.ACK) && state.status == ConnectionStatus.Setup) {
                    // Connection is establishing
                    if (state.seqN != (datagram.getAckN())) {
                        // TODO : Clean up connection and drop packet
                        throw new IllegalArgumentException("Invalid (SYN) ACK number!");
                    }

                    state.send(null, TCPDatagram.ACK);
                    state.connection.connected(state);
                } else if (flags == TCPDatagram.ACK) {
                    switch (state.status) {
                        case Setup:
                            if (state.seqN != (datagram.getAckN())) {
                                // TODO : Clean up connection and drop packet
                                throw new IllegalArgumentException("Invalid ACK number!");
                            }

                            state.status = ConnectionStatus.Established;
                            state.connection.connected(state);

                            break;
                        case Established:
                            if (datagram.getPayload() != null) {
                                state.connection.messageReceived(datagram.getPayload());
                            }
                            break;
                        case Closing:
                            if (state.seqN != datagram.getAckN()) {
                                // TODO : Clean up connection and drop packet
                                throw new IllegalArgumentException("Invalid ACK number on closing!");
                            }

                            state.connection.closed();
                            stateTable.remove(state.descriptor);
                            break;
                    }
                } else if (flags == TCPDatagram.FIN && state.status == ConnectionStatus.Established) {
                    // Closing connection
                    if (state.seqN != datagram.getAckN()) {
                        // TODO : Clean up connection and drop packet
                        throw new IllegalArgumentException("Invalid (FIN) ACK number!");
                    }

                    state.status = ConnectionStatus.Closing;
                    state.send(null, (short)(TCPDatagram.FIN | TCPDatagram.ACK));
                } else if (flags == (TCPDatagram.FIN | TCPDatagram.ACK) && state.status == ConnectionStatus.Closing) {
                    if (state.seqN != datagram.getAckN()){
                        throw new IllegalArgumentException("Invalid ACK number!");
                    }

                    state.status = ConnectionStatus.Closed;
                    state.connection.closed();
                    state.send(null, TCPDatagram.ACK);
                    stateTable.remove(state.descriptor);
                } else if (flags == TCPDatagram.RST) {
                    if (state.seqN != (datagram.getAckN())){
                        throw new IllegalArgumentException("Invalid ACK number!");
                    }

                    state.status = ConnectionStatus.Closed;
                    state.connection.closed();
                }
            }
        }
    }

    public void close(Server server) {
        for (var s : servers.entrySet()) {
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

    class ConnectionDescriptor {
        private final IPAddress localIp, remoteIp;
        private final short localPort, remotePort;

        public ConnectionDescriptor(IPAddress localIp, IPAddress remoteIp, short localPort, short remotePort) {
            this.localIp = localIp;
            this.remoteIp = remoteIp;
            this.localPort = localPort;
            this.remotePort = remotePort;
        }

        @Override
        public int hashCode() {
            return ((localIp.hashCode() * 31 + remoteIp.hashCode()) * 31 + localPort) * 31 + remotePort;
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
    }

    enum ConnectionStatus {
        Closed,
        Setup,
        Established,
        Closing,
    }

    class ConnectionState {
        private final ConnectionDescriptor descriptor;
        private final DatagramHandler handler;
        private final Connection connection;
        private ConnectionStatus status;
        private int seqN;
        private int ackN;

        public ConnectionState(ConnectionDescriptor descriptor, DatagramHandler handler, Connection connection) {
            Objects.requireNonNull(descriptor);
            Objects.requireNonNull(handler);
            Objects.requireNonNull(connection);

            this.descriptor = descriptor;
            this.handler = handler;
            this.connection = connection;
            this.status = ConnectionStatus.Setup;

            var rnd = new Random();
            this.seqN = rnd.nextInt();
        }

        public void update(TCPDatagram datagram) {
            // TODO: Error handling
            this.ackN = datagram.getSeqN();

            if (datagram.getPayload() == null || datagram.getPayload().length == 0) {
                this.ackN++;
            } else {
                this.ackN += datagram.getPayload().length;
            }
        }

        public void send(byte[] message) {
            send(message, (short) 0);
        }

        public void send(byte[] message, short flags) {
            if ((flags & (TCPDatagram.SYN | TCPDatagram.FIN | TCPDatagram.RST)) == 0) {
                flags |= TCPDatagram.ACK;
            }

            var datagram = TCPDatagram.create(descriptor.localPort, descriptor.remotePort, this.seqN, this.ackN, flags,
                    (short) 0xffff, (short) 0, message);

            var packet = IPv6Packet.create((byte)0x6, descriptor.localIp, descriptor.remoteIp, datagram.serialize());

            var destMac = handler.ethernetInterface.resolveIpAddress(descriptor.remoteIp);
            if (destMac == null) {
                destMac = handler.ethernetInterface.getDefaultRouter();
            }
            var frame = EthernetFrame.create(destMac, handler.ethernetInterface.getAddress(), EthernetFrame.TYPE_IPV6,
                    packet.serialize());

            if (message != null && message.length > 0) {
                seqN += message.length;
            } else {
                seqN++;
            }

            handler.ethernetInterface.transmit(frame.serialize());
        }

        public void close() {
            status = ConnectionStatus.Closing;
            send(null, TCPDatagram.FIN);
        }
    }
}
