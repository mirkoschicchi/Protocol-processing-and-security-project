package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.NodeConfiguration;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.NetworkImpl;
import fi.utu.protproc.group3.simulator.Simulation;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;

import java.net.UnknownHostException;

public class ServerNodeImpl extends NetworkNodeImpl implements ServerNode {
    public ServerNodeImpl(SimulationBuilderContext context, NodeConfiguration conf, NetworkImpl net) {
        super(context, conf);
    }

    @Override
    protected void packetReceived(EthernetInterface intf, byte[] pdu) throws UnknownHostException {
        super.packetReceived(intf, pdu);

        // TODO: Handle TCP handshake and server
    }
}
