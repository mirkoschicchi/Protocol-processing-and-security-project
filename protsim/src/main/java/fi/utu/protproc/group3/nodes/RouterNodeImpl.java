package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Simulation;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

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
        IPv6Packet packet = IPv6Packet.parse(frame.getPayload());
        // Decrease hop count
        // packet.setHopLimit(packet.getHopLimit() - 1);
        byte[] serializedPacket = packet.serialize();

        // Generate a Network address (IP and prefix)
        NetworkAddress destAddr = new NetworkAddress(packet.getDestinationIP(), 32);

        // Get the MAC address of the next hop
        TableRow row = this.routingTable.getRowByDestinationAddress(destAddr);
        NetworkAddress nextHop = row.getNextHop();
        // Get the MAC address of the interface to which to forward the packet
        // Filippo - Workaround to not change all the functions in the project:
        // transform the IPv6 address into a IPAddress
        IPAddress nextHopIpAddress = nextHop.getAddress();
        byte[] nextHopMac = intf.resolveIpAddress(nextHopIpAddress);

        // Update the destination of the ethernet frame
        // frame.setDestination = nextHopMac;

        // Put the update IP payload
        // frame.setPayload(serializedPacket);
        // Serialize the frame
        byte[] serializedFrame = frame.serialize();

        // Route the frame
        intf.transmit(serializedFrame);
    }

}
