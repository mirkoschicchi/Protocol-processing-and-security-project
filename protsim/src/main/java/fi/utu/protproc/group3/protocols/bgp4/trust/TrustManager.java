package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.RouterNodeImpl;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TrustManager {
    private final RouterNode router;
    private final Map<IPAddress, BGPPeerContext> peerings;
    private final Map<IPAddress, TrustAgentContext> contexts = new HashMap<>();
    private Disposable updating;

    public TrustManager(RouterNode router, Map<IPAddress, BGPPeerContext> peerings) {
        this.router = router;
        this.peerings = peerings;
    }

    public void start() {
        // Build set of all second degree peers and which routers they know about.

        // Which routers to ask about which other routers' votes
        for (var peering : peerings.values()) {
            for (var peer : peering.getSecondDegreePeers()) {
                if (!contexts.containsKey(peer)) {
                    // Here be the mistake
                    var intf = router.getInterfaces().stream()
                            .filter(i -> NetworkAddress.isMatch(i.getNetwork().getNetworkAddress(), peer))
                            .findAny()
                            .orElse(peering.getEthernetInterface());

                    contexts.put(peer, new TrustAgentContext(intf));
                }

                contexts.get(peer).peers.add(peering.getBgpIdentifier());
            }
        }

        for (var entry : contexts.entrySet()) {
            var context = entry.getValue();
            context.connection = new TrustAgentClient(context.intf, entry.getKey(), context.peers, context::handleTrustUpdate);
        }

        if (updating == null) {
            updating = Flux.interval(Duration.ofSeconds(5), Duration.ofSeconds(15))
                    .subscribe(this::updateTrustValues);
        }
    }

    private void updateTrustValues(Long i) {
        // Open connections to all second degree neighbors and ask them for all first hand routers they know, updating the total scores accordingly.
        for (var context : contexts.values()) {
            context.connection.requestScores();
        }
    }

    public void shutdown() {
        if (updating != null) {
            updating.dispose();
            updating = null;
        }
    }

    private class TrustAgentContext {
        public final EthernetInterface intf;
        public final Set<Integer> peers = new HashSet<>();
        public TrustAgentClient connection;

        public TrustAgentContext(EthernetInterface intf) {
            this.intf = intf;
        }

        public void handleTrustUpdate(Map<Integer, Double> voteResults) {
            for (var entry : voteResults.entrySet()) {
                peerings.get(entry.getKey()).addSecondDegreePeerVote(entry.getKey(), entry.getValue());
            }
        }
    }
}
