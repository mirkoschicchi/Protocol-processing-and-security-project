package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.Simulation;
import fi.utu.protproc.group3.utils.AddressGenerator;

import java.util.Collection;
import java.util.Objects;

public interface RouterNode extends NetworkNode {
    RoutingTable getRoutingTable();

    int getAutonomousSystem();
    int getInheritTrust();
}
