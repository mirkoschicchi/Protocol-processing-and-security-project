package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;

public interface SimpleNode extends NetworkNode {
    EthernetInterface getInterface();
    IPAddress getIpAddress();
}
