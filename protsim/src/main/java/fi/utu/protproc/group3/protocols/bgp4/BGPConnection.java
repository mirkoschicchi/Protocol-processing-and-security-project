package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.finitestatemachine.FSMImpl;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.logging.Logger;

public class BGPConnection extends Connection {
    private final BGPPeerContext context;

    private static final Logger LOGGER = Logger.getLogger(BGPConnection.class.getName());

    public BGPConnection(EthernetInterface ethernetInterface, BGPPeerContext context) {
        super(ethernetInterface);

        this.context = context;
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);
        context.getFsm().fire(FSMImpl.FSMEvent.Tcp_CR_Acked);
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        var bgpMessage = BGP4Message.parse(message);
        if(bgpMessage instanceof BGP4MessageOpen) {
            context.getFsm().fire(FSMImpl.FSMEvent.BGPOpen);
        } else if(bgpMessage instanceof BGP4MessageUpdate) {
            BGP4MessageUpdate updateMessage = (BGP4MessageUpdate) bgpMessage;
            for(NetworkAddress networkAddress: updateMessage.getWithdrawnRoutes()) {
                TableRow row = context.getRouter().getRoutingTable().getRowByPrefix(networkAddress);
                context.getRouter().getRoutingTable().deleteRow(row);
                LOGGER.info("Deleting row " + row.toString());
            }

            for(NetworkAddress networkAddress: updateMessage.getNetworkLayerReachabilityInformation()) {
                // Create a new row parsing also the path attributes
                TableRow newRoute = TableRow.create(networkAddress, null, 0, context.getRouter().getAutonomousSystem(), null, updateMessage.getAsPath());

                context.getRouter().getRoutingTable().insertRow(newRoute);
                LOGGER.info("Inserting row " + newRoute.toString());
            }

            context.getFsm().fire(FSMImpl.FSMEvent.UpdateMsg);

            // cast to update
            // modify the routing table
            //bgpPeering.stateMachine.fire();
        } else if(bgpMessage instanceof BGP4MessageKeepalive) {
            context.getFsm().fire(FSMImpl.FSMEvent.KeepAliveMsg);
        } else if(bgpMessage instanceof BGP4MessageNotification) {
            context.getFsm().fire(FSMImpl.FSMEvent.NotifMsg);
        }
    }

    @Override
    public void closed() {
        super.closed();
    }
}
