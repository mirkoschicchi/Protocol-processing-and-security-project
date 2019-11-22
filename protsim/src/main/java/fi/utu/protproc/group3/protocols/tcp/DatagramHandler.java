package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.Disposable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public void onMessage(byte[] pdu) {
        var frame = EthernetFrame.parse(pdu);
        if (frame.getType() == EthernetFrame.TYPE_IPV6) {
            var packet = IPv6Packet.parse(frame.getPayload());
            if (packet.getNextHeader() == 0x6) {
                var datagram = TCPDatagram.parse(packet.getPayload());
                var descriptor = new ConnectionDescriptor(packet, datagram);

                ConnectionState state = stateTable.get(descriptor);

                // TODO: Handle message, add state information to connection state class below

                // For example, something along this (could also be implemented as a FSM instead):
                // Just add whatever you need on the ConnectionState below...
//                if (state != null) {
//                    state.update(datagram);
//                }
//
//                if (datagram.getFlags() == TCPDatagram.SYN && state == null) {
//                    var server = servers.get(datagram.getDestinationPort());
//                    if (server != null) {
//                        var connection = server.accept(state);
//                        state = new ConnectionState(descriptor, this, connection);
//                        stateTable.put(descriptor, state);
//                        // TODO: Send back SYN|ACK
//                    }
//                }
            }
        }
    }

    public void listen(short port, Server server) {
        servers.put(port, server);
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
        private final IPAddress sourceIp, destIp;
        private final short sourcePort, destPort;

        public ConnectionDescriptor(IPv6Packet packet, TCPDatagram datagram) {
            this(packet.getSourceIP(), packet.getDestinationIP(), datagram.getSourcePort(), datagram.getDestinationPort());
        }

        public ConnectionDescriptor(IPAddress sourceIp, IPAddress destIp, short sourcePort, short destPort) {
            this.sourceIp = sourceIp;
            this.destIp = destIp;
            this.sourcePort = sourcePort;
            this.destPort = destPort;
        }

        @Override
        public int hashCode() {
            return ((sourceIp.hashCode() * 31 + destIp.hashCode()) * 31 + sourcePort) * 31 + destPort;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ConnectionDescriptor) {
                var other = (ConnectionDescriptor) obj;
                return other.sourcePort == sourcePort
                        && other.destPort == destPort
                        && other.sourceIp.equals(sourceIp)
                        && other.destIp.equals(destIp);
            }

            return super.equals(obj);
        }
    }

    class ConnectionState {
        private final ConnectionDescriptor descriptor;
        private final DatagramHandler handler;
        private final Connection connection;

        // TODO: Add fields required to handle Sequences and state

        public ConnectionState(ConnectionDescriptor descriptor, DatagramHandler handler, Connection connection) {
            Objects.requireNonNull(descriptor);
            Objects.requireNonNull(handler);
            Objects.requireNonNull(connection);

            this.descriptor = descriptor;
            this.handler = handler;
            this.connection = connection;
        }

        public void update(TCPDatagram datagram) {
            // TODO: Update connection state for received datagram
        }

        public void send(byte[] message) {
            // TODO: Send message to other side
            // Note: message can be null, then we just send the ACK
            // handler.ethernetInterface.transmit(...);
        }
    }
}
