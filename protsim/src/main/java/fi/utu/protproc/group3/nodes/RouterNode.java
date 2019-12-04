package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.routing.RoutingTable;

public interface RouterNode extends NetworkNode {
    RoutingTable getRoutingTable();

    int getAutonomousSystem();
    int getBGPIdentifier();
}
