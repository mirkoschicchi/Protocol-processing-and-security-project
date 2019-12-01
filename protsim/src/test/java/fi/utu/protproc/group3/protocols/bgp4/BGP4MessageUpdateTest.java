package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.AddressGenerator;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BGP4MessageUpdateTest {

    @Test
    public void createMessage() {
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
        var message = BGP4MessageUpdate.create(randomNetList, BGP4MessageUpdate.ORIGIN_FROM_IGP, aspath,
                ag.networkAddress("2001:4860:4860::8887/32").getAddress(), randomNetList);
        assertNotNull(message);

        var bytes = message.serialize();
        assertNotNull(bytes);
        assertTrue(bytes.length == message.getLength());

        var parsedMsg = (BGP4MessageUpdate) BGP4Message.parse(bytes);
        assertArrayEquals(message.getMarker(), parsedMsg.getMarker());
        assertSame(message.getType(), parsedMsg.getType());
        assertEquals(message.getLength(), parsedMsg.getLength());
        var cont = 0;
        for (NetworkAddress addr : message.getWithdrawnRoutes()) {
            assertEquals(addr.getPrefixLength(), parsedMsg.getWithdrawnRoutes().get(cont).getPrefixLength());
            assertEquals(addr.getAddress(), parsedMsg.getWithdrawnRoutes().get(cont).getAddress());
            cont++;
        }
        assertSame(message.getOrigin(), parsedMsg.getOrigin());
        assertEquals(message.getAsPath(), parsedMsg.getAsPath());
        assertEquals(message.getNextHop(), parsedMsg.getNextHop());
        cont = 0;
        for (NetworkAddress addr : message.getNetworkLayerReachabilityInformation()) {
            assertEquals(addr.getPrefixLength(), parsedMsg.getNetworkLayerReachabilityInformation().get(cont).getPrefixLength());
            assertEquals(addr.getAddress(), parsedMsg.getNetworkLayerReachabilityInformation().get(cont).getAddress());
            cont++;
        }
    }

    @Test
    public void asPathWithMultipleElements() {
        var expected = BGP4MessageUpdate.create(
                List.of(),
                BGP4MessageUpdate.ORIGIN_FROM_ESP,
                List.of(
                        List.of((short) 11, (short) 12, (short) 13),
                        List.of((short) 21, (short) 22, (short) 23),
                        List.of((short) 31, (short) 32, (short) 33)
                ),
                IPAddress.parse("fe80::1"),
                List.of()
        );

        var actual = (BGP4MessageUpdate) BGP4Message.parse(expected.serialize());

        assertEquals(3, actual.getAsPath().size());
        for (var i = 0; i < 3; i++) {
            assertEquals(3, actual.getAsPath().get(i).size());
            assertEquals((short) 1 + (i + 1) * 10, (short) actual.getAsPath().get(i).get(0));
            assertEquals((short) 2 + (i + 1) * 10, (short) actual.getAsPath().get(i).get(1));
            assertEquals((short) 3 + (i + 1) * 10, (short) actual.getAsPath().get(i).get(2));
        }
    }

    // TODO: missing good PDU to use this test
    /*@Test
    public void parseMessage() {
        var pdu = StringUtils.parseHexStream(""); // todo
        var frame = EthernetFrame.parse(pdu);
        var packet = IPv6Packet.parse(frame.getPayload());
        var datagram = TCPDatagram.parse(packet.getPayload());
        var message = BGP4Message.parse(datagram.getPayload());
        assertNotNull(message);
        assertEquals(173, message.getLength());
        assertTrue(message instanceof BGP4MessageUpdate);
        var update = (BGP4MessageUpdate) message;
        AddressGenerator ag = new AddressGenerator();
        List<List<Short>> aspath = new ArrayList<>();
        List<Short> aspathSample = new ArrayList<>();
        aspathSample.add((short)1);
        aspathSample.add((short)2);
        aspathSample.add((short)3);
        List<NetworkAddress> randomNetList = new ArrayList<>();
        for (int i=0; i < 3; i++) {
            randomNetList.add(ag.networkAddress(MessageFormat.format("2001:4860:4860::888{0}/32", i)));
            aspath.add(aspathSample);
        }
        var cont = 0;
        for (NetworkAddress addr : update.getWithdrawnRoutes()) {
            assertEquals(addr.getPrefixLength(), randomNetList.get(cont).getPrefixLength());
            assertEquals(addr.getAddress(), randomNetList.get(cont).getAddress());
            cont++;
        }
        assertSame(BGP4MessageUpdate.ORIGIN_FROM_IGP, update.getOrigin());
        assertEquals(aspath, update.getAsPath());
        NetworkAddress nh = ag.networkAddress("2001:4860:4860::8887/32");
        assertEquals(nh.getPrefixLength(), update.getNextHop().getPrefixLength());
        assertEquals(nh.getAddress(), update.getNextHop().getAddress());
        cont = 0;
        for (NetworkAddress addr : update.getNetworkLayerReachabilityInformation()) {
            assertEquals(addr.getPrefixLength(), randomNetList.get(cont).getPrefixLength());
            assertEquals(addr.getAddress(), randomNetList.get(cont).getAddress());
            cont++;
        }
    }
     */
}
