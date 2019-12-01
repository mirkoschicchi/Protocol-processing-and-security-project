package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RoutingTableTest {
    @Test
    void simpleRouting() {
        var table = RoutingTable.create();

        var r1 = TableRow.create(NetworkAddress.parse("fe80:1::/64"), IPAddress.parse("fe80:1::1"), 100, null);
        var r2 = TableRow.create(NetworkAddress.parse("fe80:2::/64"), IPAddress.parse("fe80:2::1"), 100, null);
        var r3 = TableRow.create(NetworkAddress.parse("fe80:3::/64"), IPAddress.parse("fe80:3::1"), 100, null);

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

    @Test
    void longestMatch() {
        List<Short> list1 = Arrays.asList((short)1, (short)2, (short)3);
        List<Short> list2 = Arrays.asList((short)1, (short)2);

        List<List<Short>> asPath1 = Collections.singletonList(list1);
        List<List<Short>> asPath2 = Collections.singletonList(list2);

        var table = RoutingTable.create();

        var r1 = TableRow.create(NetworkAddress.parse("fe80:1::/64"), IPAddress.parse("fe80:1::1"), 100, 0, null, asPath1);
        var r2 = TableRow.create(NetworkAddress.parse("0::/0"), IPAddress.parse("0::1"), 100, 0, null, asPath1);

        table.insertRow(r1);

        assertEquals(r1, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::")));
        assertNull(table.getRowByDestinationAddress(IPAddress.parse("fe80:1:2::1")));
        assertNull(table.getRowByDestinationAddress(IPAddress.parse("fe80:3::1")));
        assertNull(table.getRowByDestinationAddress(IPAddress.parse("fe00::1")));

        table.insertRow(r2);

        assertEquals(r1, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::")));
        assertEquals(r2, table.getRowByDestinationAddress(IPAddress.parse("fe80:1:2::1")));
        assertEquals(r2, table.getRowByDestinationAddress(IPAddress.parse("fe80:3::1")));
        assertEquals(r2, table.getRowByDestinationAddress(IPAddress.parse("fe00::1")));

        var r3 = TableRow.create(NetworkAddress.parse("fe80:1::/64"), IPAddress.parse("fe80:1::1"), 90, 0, null, asPath1);
        table.insertRow(r3);
        assertEquals(r3, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::")));

        var r4 = TableRow.create(NetworkAddress.parse("fe80:1::/64"), IPAddress.parse("fe80:1::1"), 90, 0, null, asPath2);
        table.insertRow(r4);
        assertEquals(r4, table.getRowByDestinationAddress(IPAddress.parse("fe80:1::")));
    }
}
