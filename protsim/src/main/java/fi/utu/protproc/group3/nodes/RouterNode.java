package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.Simulation;
import fi.utu.protproc.group3.utils.AddressGenerator;

import java.util.Objects;

public interface RouterNode extends NetworkNode {
    static RouterNode create(Simulation simulation, AddressGenerator generator, Network[] networks) {
        Objects.requireNonNull(simulation);
        Objects.requireNonNull(generator);
        Objects.requireNonNull(networks);

        var intfs = new EthernetInterface[networks.length];
        for (var i = 0; i < networks.length; i++) {
            intfs[i] = EthernetInterface.create(generator.ethernetAddress(), networks[i]);
            intfs[i].addInetAddress(generator.inetAddress(networks[i].getNetworkAddress()));
        }

        return new RouterNodeImpl(simulation, intfs);
    }

    RoutingTable getRoutingTable();
}
