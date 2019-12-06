package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.http.SimpleHttpClient;
import fi.utu.protproc.group3.protocols.http.SimpleHttpServer;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;
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
    public boolean nodeIsRunning() {
        return messageFlux != null;
    }

    @Override
    public void start() {
        super.start();

        if (messageFlux != null) {
            throw new UnsupportedOperationException("Client is already running.");
        }

        messageFlux = Flux.interval(Duration.ofSeconds(5))
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
                LOGGER.info("Client " + getHostname() + " trying to connect to " + dest.getHostname() + " (" + dest.getIpAddress() + ")");
                var connection = new SimpleHttpClient(this);
                connection.connect(dest.getIpAddress(), SimpleHttpServer.DEFAULT_PORT);
            }
        }
    }
}
