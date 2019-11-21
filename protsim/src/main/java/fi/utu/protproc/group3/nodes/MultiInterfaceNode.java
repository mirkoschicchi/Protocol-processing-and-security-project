package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;

import java.util.Collection;

public interface MultiInterfaceNode {
    Collection<EthernetInterface> getInterfaces();
}
