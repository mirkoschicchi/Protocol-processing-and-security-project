package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.Collection;
import java.util.Map;

public interface RouterNode extends NetworkNode {
    Configurator getConfigurator();

    int getAutonomousSystem();
    int getBGPIdentifier();
    Collection<BGPPeerContext> getPeerings() ;

    interface Configurator {
        void createPeering(EthernetInterface ethernetInterface, IPAddress neighbor, Collection<IPAddress> secondDegreeNeighbors);

        void createStaticRoute(NetworkAddress networkAddress, EthernetInterface intf, IPAddress nextHop, int metric);

        void finalizeConfiguration();
    }
}
