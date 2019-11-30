package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.finitestatemachine.FSMImpl;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.logging.Logger;

public class BGPServerConnection extends Connection {
    private BGPConnectionContext bgpConnectionContext;
    private static final Logger LOGGER = Logger.getLogger(BGPServerConnection.class.getName());
    public BGPServerConnection(EthernetInterface ethernetInterface, BGPConnectionContext bgpConnectionContext) {
        super(ethernetInterface);

        this.bgpConnectionContext = bgpConnectionContext;
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);
        //CAll fire
        this.bgpConnectionContext.fsm.fire(FSMImpl.FSMEvent.Tcp_CR_Acked);
    }

    // TODO: Move everything to callbacks
    // Starting the connection and managing the context
    // Make sre routing table works correctly
    // Messages need to be correctly saved
    // Implementing callbacks and put messages into the event context
    // Propagating update message

    // 1) RFC for IPv6 Bgp (Gabriele)
    // 2) Passing the event context correctly and move the logic into callbacks (Mirko) - replace the Integer context with BGPEventContext + put into fire message
    // 3) Make sure routing table works correctly, store AS_PATH, which AS the entry was originated from (Filippo) - takes routing with lowest metric and shortest AS_PATH
    // 4) (Stephan) Make sure that we call the server

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        var bgpMessage = BGP4Message.parse(message);
        if(bgpMessage instanceof BGP4MessageOpen) {
            bgpConnectionContext.fsm.fire(FSMImpl.FSMEvent.BGPOpen);
        } else if(bgpMessage instanceof BGP4MessageUpdate) {
            BGP4MessageUpdate updateMessage = (BGP4MessageUpdate) bgpMessage;
            for(NetworkAddress networkAddress: updateMessage.getWithdrawnRoutes()) {
                TableRow row = bgpConnectionContext.routerNode.getRoutingTable().getRowByPrefix(networkAddress);
                bgpConnectionContext.routerNode.getRoutingTable().deleteRow(row);
                LOGGER.info("Deleting row " + row.toString());
            }

            for(NetworkAddress networkAddress: updateMessage.getNetworkLayerReachabilityInformation()) {
                // Create a new row parsing also the path attributes
                TableRow newRoute = TableRow.create(networkAddress, null, 0,(short)0,(short)0,null);
                bgpConnectionContext.routerNode.getRoutingTable().insertRow(newRoute);
                LOGGER.info("Inserting row " + newRoute.toString());
            }

            bgpConnectionContext.fsm.fire(FSMImpl.FSMEvent.UpdateMsg);

            // cast to update
            // modify the routing table
            //bgpPeering.stateMachine.fire();
        } else if(bgpMessage instanceof BGP4MessageKeepalive) {
            bgpConnectionContext.fsm.fire(FSMImpl.FSMEvent.KeepAliveMsg);
        } else if(bgpMessage instanceof BGP4MessageNotification) {
            bgpConnectionContext.fsm.fire(FSMImpl.FSMEvent.NotifMsg);
        }
    }

    @Override
    public void closed() {
        super.closed();
    }
}
