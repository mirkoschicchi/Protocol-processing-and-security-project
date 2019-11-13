package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Simulation;
import fi.utu.protproc.group3.utils.IPAddress;

import java.net.UnknownHostException;

public class RouterNodeImpl extends NetworkNodeImpl implements RouterNode {
    RouterNodeImpl(Simulation simulation, EthernetInterface[] interfaces) {
        super(simulation, interfaces);
    }

    private RoutingTable routingTable;

    @Override
    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    @Override
    protected void packetReceived(EthernetInterface intf, byte[] pdu) throws UnknownHostException {
        super.packetReceived(intf, pdu);

        // Parse the bytes into an Ethernet frame object
        EthernetFrame frame = EthernetFrame.parse(pdu);
        if (frame.getType() == EthernetFrame.TYPE_IPV6) {
            IPv6Packet packet = IPv6Packet.parse(frame.getPayload());

            if (packet.getHopLimit() == 0) {
                // TODO : Error handling
                return;
            }

            // Get the MAC address of the next hop
            TableRow row = this.routingTable.getRowByDestinationAddress(packet.getDestinationIP());

            // If we found a valid routing entry
            if (row != null) {
                // Get the exit interface
                var exitIntf = row.getEInterface();

                // Get the MAC address of the interface to which to forward the packet
                IPAddress nextHop = row.getNextHop();
                byte[] nextHopMac = exitIntf.resolveIpAddress(nextHop);

                // Reassemble the IPv6 packet
                IPv6Packet newPacket = IPv6Packet.create(packet.getVersion(), packet.getTrafficClass(), packet.getFlowLabel(),
                        packet.getPayloadLength(), packet.getNextHeader(), (byte) (packet.getHopLimit() - 1),
                        packet.getSourceIP(), packet.getDestinationIP(), packet.getPayload());

                EthernetFrame newFrame = EthernetFrame.create(nextHopMac, exitIntf.getAddress(), frame.getType(), newPacket.serialize());

                // Forward the frame
                exitIntf.transmit(newFrame.serialize());
            }
        }
    }
}
