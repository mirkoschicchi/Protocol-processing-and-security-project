package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoutingTableTest {
    @Test
    void simpleRouting() {
        var table = RoutingTable.create();

        var r1 = TableRow.create(NetworkAddress.parse("fe80:1::/64"), IPAddress.parse("fe80:1::1"), 100, (short) 100, (short) 100, null);
        var r2 = TableRow.create(NetworkAddress.parse("fe80:2::/64"), IPAddress.parse("fe80:2::1"), 100, (short) 100, (short) 100, null);
        var r3 = TableRow.create(NetworkAddress.parse("fe80:3::/64"), IPAddress.parse("fe80:3::1"), 100, (short) 100, (short) 100, null);

        table.insertRow(r1);
        table.insertRow(r3);
        table.insertRow(r2);

        assertEquals(3, table.getRows().size());

        assertEquals(r1, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::")));
        assertEquals(r1, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::1")));
        assertEquals(r1, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::ffff")));
        assertEquals(r1, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::ffff:ffff:ffff:ffff")));

        assertEquals(r2, table.getRowByDestinationAddress(IPAddress.parse("fe80:2::")));
        assertEquals(r2, table.getRowByDestinationAddress(IPAddress.parse("fe80:2::1")));
        assertEquals(r2, table.getRowByDestinationAddress(IPAddress.parse("fe80:2::ffff")));
        assertEquals(r2, table.getRowByDestinationAddress(IPAddress.parse("fe80:2::ffff:ffff:ffff:ffff")));

        assertEquals(r3, table.getRowByDestinationAddress(IPAddress.parse("fe80:3::")));
        assertEquals(r3, table.getRowByDestinationAddress(IPAddress.parse("fe80:3::1")));
        assertEquals(r3, table.getRowByDestinationAddress(IPAddress.parse("fe80:3::ffff")));
        assertEquals(r3, table.getRowByDestinationAddress(IPAddress.parse("fe80:3::ffff:ffff:ffff:ffff")));
    }
}
