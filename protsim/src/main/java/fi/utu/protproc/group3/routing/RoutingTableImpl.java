package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        int longestMatch = 0;
        int greaterTrust = 0;
        int shortestAsPath = Integer.MAX_VALUE;
        int shortestMetric = Integer.MAX_VALUE;

        TableRow result = null;
        for(TableRow row: getRows()) {
            var prefixLength = row.getPrefix().getPrefixLength();
            if (prefixLength >= longestMatch
                    && NetworkAddress.isMatch(row.getPrefix(), destinationAddress)) {
                // 1) longest prefix length
                // 2) best inherit trust
                // 3) local routes (AS_PATH length)
                // 4) metric (if not 0)
                System.out.println("\nNeighbor trust: " + row.getNeighborTrust());
                if (prefixLength > longestMatch) {
                    longestMatch = prefixLength;
                    greaterTrust = row.getNeighborTrust();
                    shortestAsPath = row.getAsPathLength();
                    shortestMetric = row.getMetric() == 0 ? Integer.MAX_VALUE : row.getMetric();
                    result = row;
                } else if (row.getNeighborTrust() > greaterTrust) {
                    greaterTrust = row.getNeighborTrust();
                    shortestAsPath = row.getAsPathLength();
                    shortestMetric = row.getMetric() == 0 ? Integer.MAX_VALUE : row.getMetric();
                    result = row;
                } else if (row.getAsPathLength() < shortestAsPath) {
                    shortestAsPath = row.getAsPathLength();
                    shortestMetric = row.getMetric() == 0 ? Integer.MAX_VALUE : row.getMetric();
                    result = row;
                } else if (row.getAsPathLength() == shortestAsPath && row.getMetric() < shortestMetric) {
                    shortestMetric = row.getMetric();
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
