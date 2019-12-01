package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class RoutingTableImpl implements RoutingTable {
    // Don't know if we need it
    private short tableId;

    private Collection<TableRow> rows;

    public RoutingTableImpl() {
        this.rows = new ArrayList<TableRow>();
    }

    @Override
    public short getTableId() {
        return tableId;
    }

    @Override
    public Collection<TableRow> getRows() {
        return Collections.unmodifiableCollection(rows);
    }

    @Override
    public TableRow getRowByDestinationAddress(IPAddress destinationAddress) {
        int longestMatch = 0;
        int shortestAsPath = Integer.MAX_VALUE;
        int shortestMetric = Integer.MAX_VALUE;

        TableRow result = null;
        for(TableRow row: getRows()) {
            var prefixLength = row.getPrefix().getPrefixLength();
            if (prefixLength >= longestMatch
                    && NetworkAddress.isMatch(row.getPrefix(), destinationAddress)) {
                // 1) longest prefix length
                // 2) local routes (AS_PATH length)
                // 3) metric (if not 0)
                if (prefixLength > longestMatch) {
                    longestMatch = prefixLength;

                    shortestMetric = row.getMetric() == 0 ? Integer.MAX_VALUE : row.getMetric();
                    shortestAsPath = row.getAsPathLength();
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
        TableRow row = null;

        for(TableRow r: getRows()) {
            if(r.getPrefix().equals(prefix)) {
                row = r;
            }
        }
        return row;
    }

    @Override
    public void removeBgpEntries(int bgpIdentifier, NetworkAddress prefix) {
        if (prefix == null) {
            rows.removeIf(r -> r.getBgpPeer() == bgpIdentifier);
        } else {
            rows.removeIf(r -> r.getBgpPeer() == bgpIdentifier && r.getPrefix().equals(prefix));
        }
    }

    @Override
    public void insertRow(TableRow row) {
        rows.add(row);
    }

    @Override
    public void deleteRow(TableRow row) {
        rows.remove(row);
    }

    @Override
    public void flush() {
        rows = null;
    }

    @Override
    public void show() {
        System.out.println("Table ID: " + tableId);
        for(TableRow row : rows) {
            System.out.println(row);
        }
    }
}
