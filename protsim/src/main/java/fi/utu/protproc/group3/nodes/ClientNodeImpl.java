package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.TCPDatagram;
import fi.utu.protproc.group3.simulator.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.net.UnknownHostException;
import java.time.Duration;

public class ClientNodeImpl extends NetworkNodeImpl implements ClientNode {
    public ClientNodeImpl(SimulationBuilderContext context, NodeConfiguration configuration, Network network) {
        super(context, configuration, network);
    }

    private Disposable messageFlux;

    @Override
    public void start() {
        super.start();

        if (messageFlux != null) {
            throw new UnsupportedOperationException("Client is already running.");
        }

        messageFlux = Flux.interval(Duration.ofSeconds(3))
                .subscribe(this::sendMessage);
    }

    @Override
    public void shutdown() {
        super.shutdown();

        if (messageFlux != null) {
            messageFlux.dispose();
            messageFlux = null;
        }
    }

    private void sendMessage(long messageId) {
        var dest = simulation.getRandomServer();

//        var tcpConn = TCPClient.connect(dest.getInterfaces().iterator().next().getIpAddresses().iterator().next(), 23);
//
//        tcpConn.send();
        // TODO: Remove code below and send correct TCP packet to the destination

        var intf = getInterface();
        byte[] payload = "GET / HTTP/1.0".getBytes();
        var frame = EthernetFrame.create(
                dest.getInterface().getAddress(),
                intf.getAddress(),
                EthernetFrame.TYPE_IPV6,
                IPv6Packet.create((byte) 6, (byte) 0, 0, (byte) 6, (byte) 128,
                        getIpAddress(), dest.getIpAddress(),
                        TCPDatagram.create((short) 12345, (short) 80, 123784523, 0,
                                (short) 0, (short) 0, (short) 0, payload
                        ).serialize(getIpAddress(), dest.getIpAddress(), (byte)6, (short)(40 + 20 + payload.length))
                ).serialize()
        );

        intf.getNetwork().transmit(frame.serialize());
    }

    @Override
    protected void packetReceived(EthernetInterface intf, byte[] pdu) throws UnknownHostException {
        super.packetReceived(intf, pdu);

        // TODO: Handle reply
    }
}
