package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.protocols.tcp.TCPDatagram;
import fi.utu.protproc.group3.utils.*;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BGP4MessageUpdateTest {

    @Test
    void createMessage() {
        List<List<Short>> aspath = new ArrayList<>();
        List<Short> aspathSample = new ArrayList<>();
        aspathSample.add((short)1);
        aspathSample.add((short)2);
        aspathSample.add((short)3);
        AddressGenerator ag = new AddressGenerator();
        List<NetworkAddress> randomNetList = new ArrayList<>();
        for (int i=0; i < 3; i++) {
            randomNetList.add(ag.networkAddress(MessageFormat.format("2001:4860:4860::888{0}/32", i)));
            aspath.add(aspathSample);
        }
        var message = BGP4MessageUpdate.create(randomNetList, BGP4MessageUpdate.ORIGIN_FROM_IGP, new ASPath(aspath),
                ag.networkAddress("2001:4860:4860::8887/32").getAddress(), randomNetList);
        assertNotNull(message);

        var bytes = message.serialize();
        assertNotNull(bytes);
        assertEquals(bytes.length, message.getLength());

        var parsedMsg = (BGP4MessageUpdate) BGP4Message.parse(bytes);
        assertArrayEquals(message.getMarker(), parsedMsg.getMarker());
        assertSame(message.getType(), parsedMsg.getType());
        assertEquals(message.getLength(), parsedMsg.getLength());
        var cont = 0;
        for (NetworkAddress addr : message.getWithdrawnRoutes()) {
            assertEquals(addr.getPrefixLength(), parsedMsg.getWithdrawnRoutes().get(cont).getPrefixLength());
            assertTrue(NetworkAddress.isMatch(addr, parsedMsg.getWithdrawnRoutes().get(cont).getAddress()));
            cont++;
        }
        assertSame(message.getOrigin(), parsedMsg.getOrigin());
        assertEquals(message.getAsPath(), parsedMsg.getAsPath());
        assertEquals(message.getNextHop(), parsedMsg.getNextHop());
        cont = 0;
        for (NetworkAddress addr : message.getNetworkLayerReachabilityInformation()) {
            assertEquals(addr.getPrefixLength(), parsedMsg.getNetworkLayerReachabilityInformation().get(cont).getPrefixLength());
            assertTrue(NetworkAddress.isMatch(addr, parsedMsg.getNetworkLayerReachabilityInformation().get(cont).getAddress()));
            cont++;
        }
    }

    @Test
    void parseMessage() {
        var pdu = StringUtils.parseHexStream("2626e470895e2efc2eae45b186dd60000000008a067f20010001ffffffff152fa8993ea34d1020010001ffffffff4000e24ef7fa0477ccf600b3a4d244935b3bf2335010ffff4b1b0000ffffffffffffffffffffffffffffffff0076020000005f500100010050020018010300010002000301030001000200030103000100020003900e0024000201102001486048600000000000000000888700202001486020200148602020014860900f0012000201202001486020200148602020014860"); // todo
        var frame = EthernetFrame.parse(pdu);
        var packet = IPv6Packet.parse(frame.getPayload());
        var datagram = TCPDatagram.parse(packet.getPayload());
        var message = BGP4Message.parse(datagram.getPayload());
        assertNotNull(message);
        assertEquals(118, message.getLength());
        assertTrue(message instanceof BGP4MessageUpdate);
        var update = (BGP4MessageUpdate) message;
        List<List<Short>> aspath = new ArrayList<>();
        List<Short> aspathSample = new ArrayList<>();
        aspathSample.add((short)1);
        aspathSample.add((short)2);
        aspathSample.add((short)3);
        List<NetworkAddress> randomNetList = new ArrayList<>();
        for (int i=0; i < 3; i++) {
            randomNetList.add(NetworkAddress.parse(MessageFormat.format("2001:4860:4860::888{0}/32", i)));
            aspath.add(aspathSample);
        }
        var cont = 0;
        for (NetworkAddress addr : update.getWithdrawnRoutes()) {
            assertEquals(addr.getPrefixLength(), randomNetList.get(cont).getPrefixLength());
            assertTrue(NetworkAddress.isMatch(addr, randomNetList.get(cont).getAddress()));
            cont++;
        }
        assertSame(BGP4MessageUpdate.ORIGIN_FROM_IGP, update.getOrigin());
        assertEquals(aspath, update.getAsPath().toNetworkFormat());
        var nh = IPAddress.parse("2001:4860:4860::8887");
        assertEquals(nh, update.getNextHop());
        cont = 0;
        for (NetworkAddress addr : update.getNetworkLayerReachabilityInformation()) {
            assertEquals(addr.getPrefixLength(), randomNetList.get(cont).getPrefixLength());
            assertTrue(NetworkAddress.isMatch(addr, randomNetList.get(cont).getAddress()));
            cont++;
        }
    }
}
