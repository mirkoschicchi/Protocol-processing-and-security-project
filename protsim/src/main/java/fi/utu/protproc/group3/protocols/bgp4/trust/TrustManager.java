package fi.utu.protproc.group3.protocols.bgp4.trust;

import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class TrustManager {
    private final RouterNode router;
    private final Collection<BGPPeerContext> peerings;
    private final Map<IPAddress, EthernetInterface> interfaces;
    private Disposable updating;

    public TrustManager(RouterNode router, Collection<BGPPeerContext> peerings) {
        this.router = router;
        this.peerings = peerings;
        this.interfaces = router.getInterfaces().stream().collect(Collectors.toMap(EthernetInterface::getIpAddress, i -> i));
    }

    public void start() {
        // TODO : Build set of all second degree peers and which routers they know about.
        // TODO : Open and start trust agent connections to each of the routers calculated before

        // get correct routing interface
//        EthernetInterface ethernetInterface = interfaces.get(descriptor.getLocalIp());
//        new TrustAgentClient(router.ge, null, m -> {
//            // handle the update
//        })
        if (updating == null) {
            updating = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(15))
                    .subscribe(this::updateTrustValues);
        }
    }

    private void updateTrustValues(Long i) {
        // TODO : Open connections to all second hand neighbors and ask them for all first hand routers they know, updating the total scores accordingly.
    }

    public double getTrust(IPAddress ipAddress) {
        // TODO : Calculate and return the trust value for the given router (this method does *not* touch the network connections any longer)
        return Double.NaN;
    }

    public void shutdown() {
        if (updating != null) {
            updating.dispose();
            updating = null;
        }
    }
}
