package fi.utu.protproc.group3.routing;


import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.Collection;

public interface RoutingTable {
    static RoutingTable create() {
        return new RoutingTableImpl();
    }

    // TABLE ID
    short TABLEID_LOCAL = 255;
    short TABLEID_MAIN = 254;
    short TABLEID_DEFAULT = 253;
    short TABLEID_UNSPEC = 0;

    Collection<TableRow> getRows();

    /**
     * Get the route to go to a specific destination address
     * @param destinationAddress The destination address
     * @return A row in the routing table corresponding with that destination address
     */
    TableRow getRowByDestinationAddress(IPAddress destinationAddress);

    /**
     * Get the row corresponding to the prefix
     * @param prefix
     * @return
     */
    TableRow getRowByPrefix(NetworkAddress prefix);

    void removeBgpEntries(int bgpIdentifier, NetworkAddress prefix);

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

}
