package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.fsm.BGPCallbacksDefault;
import fi.utu.protproc.group3.protocols.bgp4.fsm.BGPStateMachine;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.ASPath;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class BGPPeerContext {
    private final RouterNode router;
    private final EthernetInterface ethernetInterface;
    private final IPAddress peer;
    private final BGPStateMachine fsm;
    private final BGPConnection connection;
    private final List<BGPPeerContext> distributionList = new ArrayList<>();
    private final Set<IPAddress> secondDegreePeers;
    private int bgpIdentifier;
    private Disposable updateSendProcess;
    private final double inherentTrust = Math.random();
    private double observedTrust = 0.5;
    private final Map<Integer, Double> secondDegreePeersVote = new HashMap<>();

    public BGPPeerContext(RouterNode router, EthernetInterface ethernetInterface, IPAddress peer, Collection<IPAddress> secondDegreePeers) {
        this.router = router;
        this.ethernetInterface = ethernetInterface;
        this.peer = peer;
        this.connection = new BGPConnection(router, this);
        this.secondDegreePeers = new HashSet<>(secondDegreePeers);
        var context = this;
        var isInitiator = ethernetInterface.getIpAddress().toArray()[15] < peer.toArray()[15];
        this.fsm = BGPStateMachine.newInstance(new BGPCallbacksDefault() {
            private boolean isFirstConnection = true;
            // Connection management
            @Override
            public void connectRemotePeer() {
                if (!isFirstConnection || isInitiator) {
                    if (connection.getState() == null || connection.getState().getStatus() != DatagramHandler.ConnectionStatus.Established) {
                        connection.connect(peer, BGPServer.PORT);
                    }
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
                        BGPStateMachine.DEFAULT_HOLD_TIME,
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

                // HACK: We clean up all routes on hold timer expiring here. Normally, the TCP implementation would
                // recognize an error in the underlying connection and we'd get a different event, but since our TCP
                // implementation cannot recognize this condition, we instead run into the hold timer and clean up
                // because of this.
                if (errorCode == BGP4MessageNotification.ERR_CODE_HOLD_TIMER_EXPIRED) {
                    deleteAllRoutes();
                }
            }

            @Override
            public void sendUpdateMessage(List<NetworkAddress> withdrawnRoutes, byte origin,
                                          ASPath asPath, IPAddress nextHop,
                                          List<NetworkAddress> networkLayerReachabilityInformation) {
                connection.send(BGP4MessageUpdate.create(withdrawnRoutes, origin, asPath, nextHop,
                        networkLayerReachabilityInformation).serialize());
            }

            // Handling for local routes
            @Override
            public void completeBGPPeerInitialization() {
                if (updateSendProcess == null) {
                    updateSendProcess = Flux.interval(Duration.ofSeconds(2), Duration.ofSeconds(30))
                            .subscribe(context::updateLocalRoutes);
                }

                isFirstConnection = false;
            }

            @Override
            public void releaseBGPResources() {
                if (updateSendProcess != null) {
                    updateSendProcess.dispose();
                    updateSendProcess = null;
                    sentRoutes.clear();
                }
            }

            @Override
            public void deleteAllRoutes() {
                var withdrawnRoutes = context.getRouter().getRoutingTable().removeBgpEntries(context.getBgpIdentifier())
                        .stream()
                        .collect(Collectors.groupingBy(r -> r.getAsPath()));

                for (var neighbour : context.getDistributionList()) {
                    for (var group : withdrawnRoutes.entrySet()) {
                        var asPath = group.getKey().add(router);

                        neighbour.getConnection().send(BGP4MessageUpdate.create(
                                group.getValue().stream().map(TableRow::getPrefix).collect(Collectors.toUnmodifiableList()),
                                BGP4MessageUpdate.ORIGIN_FROM_ESP,
                                asPath,
                                neighbour.getEthernetInterface().getIpAddress(),
                                List.of()
                        ).serialize());
                    }
                }
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

    public void fireEvent(BGPStateMachine.Event event) {
        // Squirrel Foundation seems to have some weird concurrent modification exceptions if multiple threads
        // fire an event at the same time. A per-FSM lock does not work, so we have to use a global lock here :(.
        synchronized (BGPStateMachine.LOCK) {
            fsm.fire(event);
        }
    }

    public void start() {
        if (fsm.getCurrentState() == null || fsm.getCurrentState() == BGPStateMachine.State.Idle) {
            fireEvent(BGPStateMachine.Event.ManualStart);
        }
    }

    public void stop() {
        fireEvent(BGPStateMachine.Event.ManualStop);
    }

    public Connection getConnection() {
        return connection;
    }

    private final Set<TableRow> sentRoutes = new HashSet<>();

    private void updateLocalRoutes(long updateNum) {
        if (updateNum == 0) {
            sendExistingRoutes();
        }

        var newRoutes = new ArrayList<TableRow>();
        var withdrawnRoutes = new ArrayList<TableRow>();
        var currentRoutes = router.getRoutingTable().getRows().stream()
                .filter(r -> r.getBgpPeer() == 0)
                .filter(r -> r.getInterface() != ethernetInterface)
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
                    withdrawnRoutes.stream().map(TableRow::getPrefix).collect(Collectors.toUnmodifiableList()),
                    BGP4MessageUpdate.ORIGIN_FROM_IGP,
                    new ASPath(router),
                    ethernetInterface.getIpAddress(),
                    newRoutes.stream().map(TableRow::getPrefix).collect(Collectors.toUnmodifiableList())
            );

            connection.send(msg.serialize());
        }
    }

    private void sendExistingRoutes() {
        var existingRoutesByPath = router.getRoutingTable().getRows().stream()
                .filter(r -> r.getBgpPeer() != bgpIdentifier) // skip routes sent by the peer himself
                .filter(r -> r.getBgpPeer() != 0) // skip local routes (handled in updateLocalRoutes)
                .filter(r -> r.getInterface() != ethernetInterface) // skip routes from the same interface
                .filter(r -> r.getAsPath() != null) // only send BGP routes
                .collect(Collectors.groupingBy(i -> i.getAsPath()));

        for (var path : existingRoutesByPath.entrySet()) {
            var msg = BGP4MessageUpdate.create(
                    new ArrayList<>(),
                    BGP4MessageUpdate.ORIGIN_FROM_ESP,
                    path.getKey().add(router),
                    ethernetInterface.getIpAddress(),
                    path.getValue().stream().map(TableRow::getPrefix).collect(Collectors.toUnmodifiableList())
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

    public void modifyObservedTrust(double v) {
        observedTrust = Math.min(observedTrust*v, 1.0);
    }

    public void addSecondDegreePeerVote(Integer sourceBgpIdentifier, double vote) {
        secondDegreePeersVote.put(sourceBgpIdentifier, vote);

        router.getRoutingTable().updateBgpTrust(bgpIdentifier, getTrust());
    }

    private double getVotedTrust() {
        double sum = 0;

        for (var ipAddressDoubleEntry : secondDegreePeersVote.values()) {
            sum += ipAddressDoubleEntry;
        }

        return sum / (double)secondDegreePeersVote.size();
    }

    public double getObservedTrust() {
        return observedTrust;
    }

    public double getTrust() {
        return ((inherentTrust + observedTrust) / 2.0 + getVotedTrust()) / 2.0;
    }

    public Set<IPAddress> getSecondDegreePeers() {
        return Collections.unmodifiableSet(secondDegreePeers);
    }

    public BGPStateMachine getFsm() {
        return fsm;
    }
}
