package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.protocols.bgp4.fsm.BGPStateMachine;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.utils.NetworkAddress;
import fi.utu.protproc.group3.utils.StringUtils;

import java.util.List;
import java.util.logging.Logger;

public class BGPConnection extends Connection {
    private final BGPPeerContext context;

    private static final Logger LOGGER = Logger.getLogger(BGPConnection.class.getName());

    public BGPConnection(NetworkNode node, BGPPeerContext context) {
        super(node);
        this.context = context;
    }


    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);

        // HACK HACK: Make sure the FSM is started, since it could not yet be started during the initial startup
        // Otherwise we'd have to wait for 30s for the retry timer. This is caused by our simulation starting all routers
        // at the same time and not implementing connection conflict resolution.
        if (context.getFsm().getCurrentState() == null || context.getFsm().getCurrentState() == BGPStateMachine.State.Idle) {
            context.fireEvent(BGPStateMachine.Event.AutomaticStart);
        }

        context.fireEvent(BGPStateMachine.Event.Tcp_CR_Acked);
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        var bgpMessage = BGP4Message.parse(message);

        if (bgpMessage instanceof BGP4MessageOpen) {
            BGP4MessageOpen openMessage = (BGP4MessageOpen) bgpMessage;
            context.setBgpIdentifier(openMessage.getBGPIdentifier());
            context.fireEvent(BGPStateMachine.Event.BGPOpen);
        } else if (bgpMessage instanceof BGP4MessageUpdate) {
            BGP4MessageUpdate updateMessage = (BGP4MessageUpdate) bgpMessage;
            if (!updateMessage.getAsPath().containsRouter(context.getRouter())) {
                for (NetworkAddress networkAddress : updateMessage.getWithdrawnRoutes()) {
                    context.getRouter().getRoutingTable().removeBgpEntries(context.getBgpIdentifier(), networkAddress, updateMessage.getAsPath());
                }

                for (NetworkAddress networkAddress : updateMessage.getNetworkLayerReachabilityInformation()) {
                    // Create a new row parsing also the path attributes
                    TableRow newRoute = TableRow.create(networkAddress, updateMessage.getNextHop(), 0,
                            context.getBgpIdentifier(), context.getEthernetInterface(), updateMessage.getAsPath(), context.getTrust());
                    context.getRouter().getRoutingTable().insertRow(newRoute);
                    LOGGER.fine(context.getRouter().getHostname() + ": inserting row " + newRoute.toString());
                }

                context.fireEvent(BGPStateMachine.Event.UpdateMsg);

                var asPath = updateMessage.getAsPath().add(context.getRouter());
                for (var neighbour : context.getDistributionList()) {
                    neighbour.getConnection().send(BGP4MessageUpdate.create(
                            updateMessage.getWithdrawnRoutes(),
                            BGP4MessageUpdate.ORIGIN_FROM_ESP,
                            asPath,
                            neighbour.getEthernetInterface().getIpAddress(),
                            updateMessage.getNetworkLayerReachabilityInformation()
                    ).serialize());
                }
            }
        } else if (bgpMessage instanceof BGP4MessageKeepalive) {
            context.modifyObservedTrust(1.05);
            context.fireEvent(BGPStateMachine.Event.KeepAliveMsg);
        } else if (bgpMessage instanceof BGP4MessageNotification) {
            context.fireEvent(BGPStateMachine.Event.NotifMsg);
        }
    }

    @Override
    public void closed() {
        context.modifyObservedTrust(0.9);
        super.closed();
    }
}
