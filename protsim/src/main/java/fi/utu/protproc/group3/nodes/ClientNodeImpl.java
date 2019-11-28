package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
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
    public boolean nodeIsRunning() {
        return messageFlux != null;
    }

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
        if (nodeIsRunning()) {
            var dest = simulation.getRandomServer();
            if (dest != null) {
                getInterface().getTCPHandler().connect(new SimpleHttpClientConnection(getInterface()), dest.getIpAddress(), (short) 80);
            }
        }
    }

    static class SimpleHttpClientConnection extends Connection {
        public SimpleHttpClientConnection(EthernetInterface ethernetInterface) {
            super(ethernetInterface);
        }

        @Override
        public void connected(DatagramHandler.ConnectionState connectionState) {
            super.connected(connectionState);

            send("GET / HTTP/1.0".getBytes());
        }

        @Override
        public void messageReceived(byte[] message) {
            super.messageReceived(message);

            close();
        }
    }
}
