package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.http.SimpleHttpClient;
import fi.utu.protproc.group3.protocols.http.SimpleHttpServer;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.logging.Logger;

public class ClientNodeImpl extends NetworkNodeImpl implements ClientNode {
    private static final Logger LOGGER = Logger.getLogger(ClientNodeImpl.class.getName());

    public ClientNodeImpl(SimulationBuilderContext context, NodeConfiguration configuration, Network network) {
        super(context, configuration, network);
    }

    private Disposable messageFlux;

    @Override
    public boolean isOnline() {
        return messageFlux != null;
    }

    @Override
    public void start() {
        super.start();

        if (messageFlux != null) {
            throw new UnsupportedOperationException("Client is already running.");
        }

        var initialDelay = Math.random() * 5000 + 3000;
        messageFlux = Flux.interval(Duration.ofMillis((long) initialDelay), Duration.ofSeconds(5))
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

    private SimpleHttpClient lastConnection;
    private IPAddress lastDestination;
    private void sendMessage(long messageId) {
        if (isOnline()) {
            if (lastConnection != null && !lastConnection.wasSuccessful()) {
                LOGGER.warning("Client " + getHostname() + " timed out connecting " + lastDestination);
                lastConnection = null;
            }

            var dest = simulation.getRandomServer();
            if (dest != null) {
                LOGGER.fine("Client " + getHostname() + " trying to connect to " + dest.getHostname() + " (" + dest.getIpAddress() + ")");
                var connection = new SimpleHttpClient(this);
                connection.connect(dest.getIpAddress(), SimpleHttpServer.DEFAULT_PORT);

                lastConnection = connection;
                lastDestination = dest.getIpAddress();
            }
        }
    }
}
