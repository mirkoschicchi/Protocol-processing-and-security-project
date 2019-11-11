package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.NetworkAddress;

/**
 * This represents a row in the routing table
 */
public interface TableRow {
    static TableRow create(NetworkAddress prefix, NetworkAddress nextHop, int metric,
                           short tos, short scope, EthernetInterface eInterface) {
        return new TableRowImpl(prefix, nextHop, metric, tos, scope, eInterface);
    }

    // From cat /etc/iproute2/rt_protos
    short PROTOCOL_BGP = 186;

    // TOS
    short TOS_DEFAULT = 0x00;
    short TOS_AF11 = 0x28;
    short TOS_AF12 = 0x30;
    short TOS_AF13 = 0x38;
    short TOS_AF21 = 0x48;
    short TOS_AF22 = 0x50;
    short TOS_AF23 = 0x58;
    short TOS_AF31 = 0x68;
    short TOS_AF32 = 0x70;
    short TOS_AF33 = 0x78;
    short TOS_AF41 = 0x88;
    short TOS_AF42 = 0x90;
    short TOS_AF43 = 0x98;

    // SCOPE
    short SCOPE_GLOBAL = 0;
    short SCOPE_NOWHERE = 255;
    short SCOPE_HOST = 254;
    short SCOPE_LINK = 253;

    NetworkAddress getPrefix();
    NetworkAddress getNextHop();
    int getMetric();
    short getTos();
    short getScope();
    EthernetInterface getEInterface();

    void update(NetworkAddress prefix, NetworkAddress nextHop, int metric,
                short tos, short scope, EthernetInterface eInterface);

    void show();
}
