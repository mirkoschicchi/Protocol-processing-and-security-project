package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class RoutingTableImpl implements RoutingTable {
    private List<TableRow> rows = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
    public ArrayList<TableRow> removeBgpEntries(int bgpIdentifier, NetworkAddress prefix) {
        var wl = lock.writeLock();
        wl.lock();
        try {
            var result = new ArrayList<TableRow>();
            for (var i = rows.size() - 1; i >= 0; i--) {
                TableRow row = rows.get(i);
                if (row.getBgpPeer() == bgpIdentifier && (prefix == null || row.getPrefix().equals(prefix))) {
                    result.add(row);
                    rows.remove(i);
                }
            }

            return result;
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
            for (var route : rows) {
                if (route.equals(row)) {
                    return;
                }
            }

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
