package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.finitestatemachine.BGPEventContext;
import fi.utu.protproc.group3.finitestatemachine.FSMImpl;
import fi.utu.protproc.group3.finitestatemachine.InternalFSMCallbacksImpl;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BGPPeerContext {
    private final RouterNode router;
    private final EthernetInterface ethernetInterface;
    private final IPAddress peer;
    private final UntypedStateMachine fsm;
    private final BGPConnection connection;
    private final List<BGPPeerContext> distributionList = new ArrayList<>();
    private int bgpIdentifier;
    private Disposable updateSendProcess;

    public BGPPeerContext(RouterNode router, EthernetInterface ethernetInterface, IPAddress peer) {
        this.router = router;
        this.ethernetInterface = ethernetInterface;
        this.peer = peer;
        this.connection = new BGPConnection(ethernetInterface, this);

        var context = this;
        var isInitiator = ethernetInterface.getIpAddress().toArray()[15] < peer.toArray()[15];
        this.fsm = FSMImpl.newInstance(new InternalFSMCallbacksImpl() {
            // Connection management
            @Override
            public void connectRemotePeer() {
                if (isInitiator) {
                    connection.connect(peer, BGPServer.PORT);
                }
            }

            @Override
            public void dropTCPConnection() {
                connection.close();
            }

            @Override
            public void closeBGPConnection() {
                connection.close();
            }

            // Deliver messages
            @Override
            public void sendOpenMessage() {
                connection.send(BGP4MessageOpen.create(
                        (short) router.getAutonomousSystem(),
                        FSMImpl.DEFAULT_HOLD_TIME,
                        router.getBGPIdentifier()
                ).serialize());
            }

            @Override
            public void sendKeepaliveMessage() {
                connection.send(BGP4MessageKeepalive.create().serialize());
            }

            @Override
            public void sendNotificationMessage(byte errorCode, byte subErrorCode, byte[] data) {
                connection.send(BGP4MessageNotification.create(errorCode, subErrorCode, data).serialize());
            }

            // Handling for local routes
            @Override
            public void completeBGPPeerInitialization() {
                if (updateSendProcess == null) {
                    updateSendProcess = Flux.interval(Duration.ofSeconds(2), Duration.ofSeconds(30))
                            .subscribe(context::updateLocalRoutes);
                }
            }

            @Override
            public void deleteAllRoutes() {
                context.getRouter().getRoutingTable().removeBgpEntries(context.getBgpIdentifier(), null);
            }
        });
    }

    public RouterNode getRouter() {
        return router;
    }

    public EthernetInterface getEthernetInterface() {
        return ethernetInterface;
    }

    public IPAddress getPeer() {
        return peer;
    }

    public void fireEvent(FSMImpl.FSMEvent event) {
        synchronized (fsm) {
            fsm.fire(event);
        }
    }

    public void fireEvent(FSMImpl.FSMEvent event, BGPEventContext context) {
        synchronized (fsm) {
            fsm.fire(event, context);
        }
    }

    public void start() {
        fireEvent(FSMImpl.FSMEvent.ManualStart);
    }

    public void stop() {
        if (updateSendProcess != null) {
            updateSendProcess.dispose();
            updateSendProcess = null;
        }

        fireEvent(FSMImpl.FSMEvent.ManualStop);
    }

    public Connection getConnection() {
        return connection;
    }

    private final Set<TableRow> sentRoutes = new HashSet<>();

    private void updateLocalRoutes(long updateNum) {
        var newRoutes = new ArrayList<TableRow>();
        var withdrawnRoutes = new ArrayList<TableRow>();
        var currentRoutes = router.getRoutingTable().getRows().stream()
                .filter(r -> r.getBgpPeer() == 0)
                .filter(r -> r.getEInterface() != ethernetInterface)
                .collect(Collectors.toUnmodifiableSet());

        for (var route : currentRoutes) {
            if (sentRoutes.add(route)) {
                newRoutes.add(route);
            }
        }

        for (var route : sentRoutes) {
            if (!currentRoutes.contains(route)) {
                withdrawnRoutes.add(route);
                sentRoutes.remove(route);
            }
        }

        if (newRoutes.size() + withdrawnRoutes.size() > 0) {
            var msg = BGP4MessageUpdate.create(
                    withdrawnRoutes.stream().map(r -> r.getPrefix()).collect(Collectors.toUnmodifiableList()),
                    BGP4MessageUpdate.ORIGIN_FROM_IGP,
                    List.of(List.of((short) router.getAutonomousSystem()), List.of((short) router.getBGPIdentifier())),
                    ethernetInterface.getIpAddress(),
                    newRoutes.stream().map(r -> r.getPrefix()).collect(Collectors.toUnmodifiableList())
            );

            connection.send(msg.serialize());
        }
    }

    public List<BGPPeerContext> getDistributionList() {
        return distributionList;
    }

    public int getBgpIdentifier() {
        return bgpIdentifier;
    }

    public void setBgpIdentifier(int bgpIdentifier) {
        this.bgpIdentifier = bgpIdentifier;
    }
}
