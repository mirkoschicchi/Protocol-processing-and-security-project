package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class RoutingTableImpl implements RoutingTable {
    private Collection<TableRow> rows = new ArrayList<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Collection<TableRow> getRows() {
        var rl = lock.readLock();
        rl.lock();
        try {
            return rows.stream().collect(Collectors.toUnmodifiableList());
        } finally {
            rl.unlock();
        }
    }

    @Override
    public TableRow getRowByDestinationAddress(IPAddress destinationAddress) {
        int longestMatch = -1;
        double shortestMetric = Integer.MAX_VALUE;

        TableRow result = null;
        for(TableRow row: getRows()) {
            var prefixLength = row.getPrefix().getPrefixLength();
            if (prefixLength >= longestMatch
                    && NetworkAddress.isMatch(row.getPrefix(), destinationAddress)) {
                // 1) longest prefix length
                // 2) calculated metric
                if (prefixLength > longestMatch) {
                    longestMatch = prefixLength;
                    shortestMetric = row.getCalculatedMetric();
                    result = row;
                } else if (row.getCalculatedMetric() < shortestMetric) {
                    shortestMetric = row.getCalculatedMetric();
                    result = row;
                }
            }
        }

        return result;
    }

    @Override
    public TableRow getRowByPrefix(NetworkAddress prefix) {
        TableRow result = null;
        for(TableRow r : getRows()) {
            if(r.getPrefix().equals(prefix)) {
                result = r;
            }
        }
        return result;
    }

    @Override
    public void removeBgpEntries(int bgpIdentifier, NetworkAddress prefix) {
        var wl = lock.writeLock();
        wl.lock();
        try {
            if (prefix == null) {
                rows.removeIf(r -> r.getBgpPeer() == bgpIdentifier);
            } else {
                rows.removeIf(r -> r.getBgpPeer() == bgpIdentifier && r.getPrefix().equals(prefix));
            }
        } finally {
            wl.unlock();
        }
    }

    @Override
    public void updateBgpTrust(int bgpIdentifier, double trust) {
        var rl = lock.readLock();
        rl.lock();
        try {
            getRows().stream().filter(r -> r.getBgpPeer() == bgpIdentifier)
                    .forEach(r -> r.setTrust(trust));
        } finally {
            rl.unlock();
        }
    }

    @Override
    public void insertRow(TableRow row) {
        var wl = lock.writeLock();
        wl.lock();
        try {
            rows.add(row);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public void deleteRow(TableRow row) {
        var wl = lock.writeLock();
        wl.lock();
        try {
            rows.remove(row);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public void flush() {
        rows = null;
    }
}
