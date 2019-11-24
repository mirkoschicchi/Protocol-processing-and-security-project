package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;

import java.util.Collection;

public interface MultiInterfaceNode extends NetworkNode {
    Collection<EthernetInterface> getInterfaces();
}
