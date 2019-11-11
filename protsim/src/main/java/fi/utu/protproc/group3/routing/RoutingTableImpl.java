package fi.utu.protproc.group3.routing;

import java.util.List;

public class RoutingTableImpl implements RoutingTable {
    private short tableId;
    private List<TableRow> rows;

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
