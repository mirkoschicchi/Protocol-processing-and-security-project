package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.simulator.*;

import java.net.UnknownHostException;

public class ServerNodeImpl extends NetworkNodeImpl implements ServerNode {
    public ServerNodeImpl(SimulationBuilderContext context, NodeConfiguration conf, Network net) {
        super(context, conf, net);
    }

    @Override
    protected void packetReceived(EthernetInterface intf, byte[] pdu) throws UnknownHostException {
        super.packetReceived(intf, pdu);

        // TODO: Handle TCP handshake and server
    }
}
