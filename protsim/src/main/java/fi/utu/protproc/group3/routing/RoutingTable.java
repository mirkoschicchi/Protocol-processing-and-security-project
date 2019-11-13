package fi.utu.protproc.group3.routing;


import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.Collection;

public interface RoutingTable {
    static RoutingTable create() {throw new UnsupportedOperationException();}

    // TABLE ID
    short TABLEID_LOCAL = 255;
    short TABLEID_MAIN = 254;
    short TABLEID_DEFAULT = 253;
    short TABLEID_UNSPEC = 0;

    short getTableId();
    Collection<TableRow> getRows();

    /**
     * Get the route to go to a specific destination address
     * @param destinationAddress The destination address
     * @return A row in the routing table corresponding with that destination address
     */
    TableRow getRowByDestinationAddress(IPAddress destinationAddress);

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
