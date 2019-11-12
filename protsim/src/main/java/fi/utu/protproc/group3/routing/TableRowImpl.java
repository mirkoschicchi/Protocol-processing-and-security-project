package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.NetworkAddress;

public class TableRowImpl implements TableRow {
    private NetworkAddress prefix;
    private NetworkAddress nextHop;
    private int metric;
    private short tos;
    private short scope;
    private EthernetInterface eInterface;

    public TableRowImpl(NetworkAddress prefix, NetworkAddress nextHop,
                        int metric, short tos, short scope, EthernetInterface eInterface) {
        this.prefix = prefix;
        this.nextHop = nextHop;
        this.metric = metric;
        this.tos = tos;
        this.scope = scope;
        this.eInterface = eInterface;
    }

    @Override
    public NetworkAddress getPrefix() {
        return prefix;
    }

    @Override
    public NetworkAddress getNextHop() {
        return nextHop;
    }

    @Override
    public int getMetric() {
        return metric;
    }

    @Override
    public short getTos() {
        return tos;
    }

    @Override
    public short getScope() {
        return scope;
    }

    @Override
    public EthernetInterface getEInterface() {
        return eInterface;
    }

    @Override
    public void update(NetworkAddress prefix, NetworkAddress nextHop, int metric, short tos,
                       short scope, EthernetInterface eInterface) {
        this.prefix = prefix;
        this.nextHop = nextHop;
        this.metric = metric;
        this.tos = tos;
        this.scope = scope;
        this.eInterface = eInterface;
    }

    @Override
    public void show() {
        System.out.println("Prefix:" + prefix.toString() + " | Next Hop:" + nextHop.toString() + " | metric:" + metric +
                " | TOS:" + tos + " | scope:" + scope + " | Interface:" + eInterface.toString());
    }

}
