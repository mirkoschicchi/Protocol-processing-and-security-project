package fi.utu.protproc.group3.routing;


import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;

public interface RoutingTable {
    static RoutingTable create() {throw new UnsupportedOperationException();}

    // TABLE ID
    short TABLEID_LOCAL = 255;
    short TABLEID_MAIN = 254;
    short TABLEID_DEFAULT = 253;
    short TABLEID_UNSPEC = 0;

    short getTableId();
    List<TableRow> getRows();

    TableRow getRowByIndex(int rowIndex);

    /**
     * Insert a new row in the table
     * @param row The row to be inserted
     */
    void insertRow(TableRow row);

    /**
     * Delete a particular row from the table
     * @param row The row to be deleted
     */
    void deleteRow(TableRow row);

    /**
     * Delete the content of the entire table
     */
    void flush();

    /**
     * Show entire content of routing table
     */
    void show();
}
