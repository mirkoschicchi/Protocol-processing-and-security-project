package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import org.jetbrains.annotations.NotNull;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TrustManager {
    private final RouterNode router;
    private final Map<IPAddress, BGPPeerContext> peerings;
    private final Map<IPAddress, EthernetInterface> interfaces;
    private final Map<EthernetInterface, Set<IPAddress>> peersNeighbors = new HashMap<>();
    private Disposable updating;
    private final List<TrustAgentClient> trustAgentClients = new ArrayList<>();

    public TrustManager(RouterNode router, Map<IPAddress, BGPPeerContext> peerings) {
        this.router = router;
        this.peerings = peerings;
        this.interfaces = router.getInterfaces().stream().collect(Collectors.toMap(EthernetInterface::getIpAddress, i -> i));
    }

    public void start() {
        // Build set of all second degree peers and which routers they know about.
        for (Map.Entry<IPAddress, BGPPeerContext> peeringsEntry : peerings.entrySet()) {
            Set<IPAddress> secondDegreePeers = peeringsEntry.getValue().getSecondDegreePeers();
            if (secondDegreePeers != null) {
                peersNeighbors.put(peeringsEntry.getValue().getEthernetInterface(), secondDegreePeers); // here I have the second degree peers of one peer and my ethernet interface
            }
        }

        // Open and start trust agent connections to each of the routers calculated before
        for (Map.Entry<EthernetInterface, Set<IPAddress>> entry : peersNeighbors.entrySet()) {
            // Add a new connection passing my ethernet interface (taken from the peering BGPPeerContext) as first parameter
            // and the second degree peers as second parameter
            TrustAgentClient trustAgentClient = new TrustAgentClient(entry.getKey(), entry.getValue(), ipAddressDoubleMap -> {
                for (Map.Entry<IPAddress, Double> ipAddressDoubleEntry : ipAddressDoubleMap.entrySet()) {
                    peerings.get(ipAddressDoubleEntry.getKey()).addSecondDegreePeerVote(ipAddressDoubleEntry.getKey(), ipAddressDoubleEntry.getValue());
                }
            });

            // Start the connection with the peers of my peer
            for (IPAddress ipAddress : entry.getValue()) {
                trustAgentClient.connect(ipAddress, TrustAgentServer.PORT);
            }

            // Save the connections in a variable since we need to access them
            trustAgentClients.add(trustAgentClient);
        }

        if (updating == null) {
            updating = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(15))
                    .subscribe(this::updateTrustValues);
        }
    }

    private void updateTrustValues(Long i) {
        // Open connections to all second degree neighbors and ask them for all first hand routers they know, updating the total scores accordingly.
        for (TrustAgentClient trustAgentClient : trustAgentClients) {
            trustAgentClient.requestScores();
        }
    }

    public double getTrust(IPAddress ipAddress) {
        // Calculate and return the trust value for the given router (this method does *not* touch the network connections any longer)
        // In this case it should be enough to get the trust (observed + inherent) and voted trust
        return (peerings.get(ipAddress).getTrust() + peerings.get(ipAddress).getVotedTrust()) / 2.0;
    }

    public void shutdown() {
        if (updating != null) {
            updating.dispose();
            updating = null;
        }
    }
}
