package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.ArrayList;
import java.util.List;

public class RoutingTableImpl implements RoutingTable {
    // Don't know if we need it
    private short tableId;

    private List<TableRow> rows;

    public RoutingTableImpl() {
        this.rows = new ArrayList<TableRow>();
    }

    @Override
    public short getTableId() {
        return tableId;
    }

    @Override
    public List<TableRow> getRows() {
        return rows;
    }

    @Override
    public TableRow getRowByIndex(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public TableRow getRowByDestinationAddress(NetworkAddress destinationAddress) {
        for(TableRow row: getRows()) {
            if(destinationAddress.equals(row.getPrefix())) {
                return row;
            }
        }
        return null;
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
