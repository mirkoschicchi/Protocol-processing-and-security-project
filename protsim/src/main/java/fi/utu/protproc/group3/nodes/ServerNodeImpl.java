package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Simulation;

public class ServerNodeImpl extends NetworkNodeImpl implements ServerNode {
    public ServerNodeImpl(Simulation simulation, EthernetInterface intf) {
        super(simulation, new EthernetInterface[]{intf});
    }

    @Override
    protected void packetReceived(EthernetInterface intf, byte[] pdu) {
        super.packetReceived(intf, pdu);

        // TODO: Handle TCP handshake and server
    }
}
