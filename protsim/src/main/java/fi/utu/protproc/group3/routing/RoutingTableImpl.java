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
        int longestMatch = -1;
        TableRow routeRow = null;
        for(TableRow row: getRows()) {
            int matchLength = NetworkAddress.matchLength(row.getPrefix(), destinationAddress);
            if(matchLength > longestMatch) {
                longestMatch = matchLength;
                routeRow = row;
            }
        }
        return routeRow;
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
            row.show();
        }
    }
}
