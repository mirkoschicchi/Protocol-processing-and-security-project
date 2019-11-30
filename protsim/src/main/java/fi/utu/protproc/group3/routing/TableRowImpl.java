package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;


public class TableRowImpl implements TableRow {
    private NetworkAddress prefix;
    private IPAddress nextHop;
    private int metric;
    private EthernetInterface eInterface;
    private int bgpPeer;
    private List<List<Short>> asPath;

    public TableRowImpl(NetworkAddress prefix, IPAddress nextHop,
                        int metric, EthernetInterface eInterface) {
        this.prefix = prefix;
        this.nextHop = nextHop;
        this.metric = metric;
        this.eInterface = eInterface;
    }

    public TableRowImpl(NetworkAddress prefix, IPAddress nextHop, int metric, int bgpPeer,
                        EthernetInterface eInterface, List<List<Short>> asPath) {
        this.prefix = prefix;
        this.nextHop = nextHop;
        this.metric = metric;
        this.bgpPeer = bgpPeer;
        this.eInterface = eInterface;
        this.asPath = asPath;
    }

    @Override
    public NetworkAddress getPrefix() {
        return prefix;
    }

    @Override
    public IPAddress getNextHop() {
        return nextHop;
    }

    @Override
    public int getMetric() {
        return metric;
    }

    @Override
    public int getBgpPeer() {
        return bgpPeer;
    }

    @Override
    public EthernetInterface getEInterface() {
        return eInterface;
    }

    @Override
    public List<List<Short>> getAsPath() {
        return asPath;
    }

    @Override
    public int getAsPathLength() {
        return asPath != null ? asPath.get(0).size() : 0;
    }

    @Override
    public void show() {
        System.out.println("Prefix:" + prefix.toString() + " | Next Hop:" + nextHop.toString() + " | metric:" + metric +
                " | Interface:" + eInterface.toString());
    }

    @Override
    public String toString() {
        if (eInterface == null) {
            return "Prefix:" + prefix.toString() + " | Next Hop:" + nextHop.toString() + " | metric:" + metric +
                    " | Interface: null";
        } else {
            return "Prefix:" + prefix.toString() + " | Next Hop:" + nextHop.toString() + " | metric:" + metric +
                    "| Interface:" + eInterface.toString();
        }
    }
}
