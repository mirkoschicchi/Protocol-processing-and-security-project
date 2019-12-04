package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;
import java.util.Objects;


public class TableRowImpl implements TableRow {
    private NetworkAddress prefix;
    private IPAddress nextHop;
    private int metric;
    private EthernetInterface eInterface;
    private int bgpPeer;
    private List<List<Short>> asPath;
    private double neighborTrust;

    public TableRowImpl(NetworkAddress prefix, IPAddress nextHop,
                        int metric, EthernetInterface eInterface) {
        this.prefix = prefix;
        this.nextHop = nextHop;
        this.metric = metric;
        this.eInterface = eInterface;
    }

    public TableRowImpl(NetworkAddress prefix, IPAddress nextHop, int metric, int bgpPeer,
                        EthernetInterface eInterface, List<List<Short>> asPath, double neighborTrust) {
        this.prefix = prefix;
        this.nextHop = nextHop;
        this.metric = metric;
        this.bgpPeer = bgpPeer;
        this.eInterface = eInterface;
        this.asPath = asPath;
        this.neighborTrust = neighborTrust;
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
    public double getCalculatedMetric() {
        return metric == 0 ? (this.getAsPathLength() * 100) / neighborTrust : (metric * (this.getAsPathLength() * 100)) / neighborTrust;
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
    public String toString() {
        var result = new StringBuilder();
        result.append(prefix);
        if (eInterface != null) {
            result.append(" dev " + eInterface.getNetwork().getNetworkName());
        }

        if (nextHop != null) {
            result.append(" via " + nextHop);
        }

        if (metric > 0) {
            result.append(" metric " + metric);
        }

        if (bgpPeer != 0) {
            result.append(" peer ").append(bgpPeer);
        }

        if (asPath != null && asPath.size() > 0) {
            result.append(" as_path AS").append(String.join(
                    ", AS",
                    (Iterable<String>) asPath.get(0).stream().map(Object::toString)::iterator)
            );
        }

        return result.toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TableRowImpl) {
            var other = (TableRowImpl) obj;
            return Objects.equals(other.nextHop, nextHop)
                    && Objects.equals(other.eInterface, eInterface)
                    && other.metric == metric
                    && Objects.equals(other.prefix, prefix);
        }

        return super.equals(obj);
    }
}
