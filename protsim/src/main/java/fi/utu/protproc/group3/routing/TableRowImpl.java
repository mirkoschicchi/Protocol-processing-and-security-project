package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.ASPath;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class TableRowImpl implements TableRow {
    private final NetworkAddress prefix;
    private final IPAddress nextHop;
    private final int metric;
    private final EthernetInterface eInterface;
    private int bgpPeer;
    private ASPath asPath;
    private double neighborTrust;

    public TableRowImpl(NetworkAddress prefix, IPAddress nextHop,
                        int metric, EthernetInterface eInterface) {
        this.prefix = prefix;
        this.nextHop = nextHop;
        this.metric = metric;
        this.eInterface = eInterface;
        this.asPath = ASPath.LOCAL;
    }

    public TableRowImpl(NetworkAddress prefix, IPAddress nextHop, int metric, int bgpPeer,
                        EthernetInterface eInterface, ASPath asPath, double neighborTrust) {
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
        return metric != 0 ? metric : (asPath.length() * 100) / Math.max(0.0001, neighborTrust);
    }

    @Override
    public int getBgpPeer() {
        return bgpPeer;
    }

    @Override
    public EthernetInterface getInterface() {
        return eInterface;
    }

    @Override
    public ASPath getAsPath() {
        return asPath;
    }

    @Override
    public void setTrust(double trust) {
        this.neighborTrust = trust;
    }

    @Override
    public String toString() {
        var result = new StringBuilder();
        result.append(prefix);
        if (eInterface != null) {
            result.append(" dev ").append(eInterface.getNetwork().getNetworkName());
        }

        if (nextHop != null) {
            result.append(" via ").append(nextHop);
        }

        if (metric > 0) {
            result.append(" metric ").append(metric);
        }

        if (bgpPeer != 0) {
            result.append(" peer ").append(bgpPeer);
        }

        if (asPath != null && asPath.length() > 0) {
            result.append(" as_path ").append(asPath);
        }

        return result.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(eInterface, nextHop, prefix, bgpPeer, asPath);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TableRowImpl) {
            var other = (TableRowImpl) obj;
            return Objects.equals(other.nextHop, nextHop)
                    && Objects.equals(other.eInterface, eInterface)
                    && Objects.equals(other.prefix, prefix)
                    && Objects.equals(other.asPath, asPath)
                    && bgpPeer == other.bgpPeer;
        }

        return super.equals(obj);
    }
}
