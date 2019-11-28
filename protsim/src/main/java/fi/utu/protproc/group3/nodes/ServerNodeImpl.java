package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.Server;
import fi.utu.protproc.group3.simulator.*;

import java.io.UnsupportedEncodingException;

public class ServerNodeImpl extends NetworkNodeImpl implements ServerNode {
    private Server server;

    public ServerNodeImpl(SimulationBuilderContext context, NodeConfiguration conf, Network net) {
        super(context, conf, net);
    }

    @Override
    public void start() {
        super.start();

        this.server = Server.listen(getInterface(), (short) 80, SimpleHttpServerConnection.class);
    }

    @Override
    public void shutdown() {
        server.close();

        super.shutdown();
    }

    public static class SimpleHttpServerConnection extends Connection {
        public SimpleHttpServerConnection(EthernetInterface ethernetInterface) {
            super(ethernetInterface);
        }

        @Override
        public void messageReceived(byte[] message) {
            super.messageReceived(message);

            try {
                var reply = "<h1>Hello from " + ethernetInterface.getHost().getHostname() + "</h1>" +
                        "<p>Your request:</p>" +
                        "<pre>" + new String(message, "UTF-8") + "</pre>";

                send(reply.getBytes());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
