package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.Simulation;
import fi.utu.protproc.group3.utils.AddressGenerator;

import java.util.Objects;

public interface ServerNode extends NetworkNode {
    static ServerNode create(Simulation simulation, AddressGenerator generator, Network network) {
        Objects.requireNonNull(simulation);
        Objects.requireNonNull(network);
        Objects.requireNonNull(generator);

        var addr = generator.ipAddress(network.getNetworkAddress());
        var intf = EthernetInterface.create(generator.ethernetAddress(), network);
        intf.addIpAddress(addr);

        return new ServerNodeImpl(simulation, intf);
    }
}
