package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.http.SimpleHttpServer;
import fi.utu.protproc.group3.protocols.tcp.Server;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;

public class ServerNodeImpl extends NetworkNodeImpl implements ServerNode {
    private Server httpServer;

    public ServerNodeImpl(SimulationBuilderContext context, NodeConfiguration conf, Network net) {
        super(context, conf, net);
    }

    @Override
    public void start() {
        super.start();

        this.httpServer = Server.listen(getInterface(), SimpleHttpServer.DEFAULT_PORT, SimpleHttpServer.class);
    }

    @Override
    public void shutdown() {
        httpServer.shutdown();

        super.shutdown();
    }

}
