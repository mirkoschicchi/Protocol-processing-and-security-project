package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.protocols.EthernetFrame;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EthernetInterfaceTest {
    private static final byte[] sourceMac = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    private static final byte[] destMac = {0x10, 0x11, 0x12, 0x13, 0x14, 0x15};

    @Test
    public void transmitAndReceiveFrame() {
        var net = Network.create();
        var intf1 = EthernetInterface.create(sourceMac, net);
        var intf2 = EthernetInterface.create(destMac, net);

        var frame = EthernetFrame.create(destMac, sourceMac, EthernetFrame.TYPE_IPV6, new byte[] { 0x00 });

        var buf = frame.serialize();
        intf1.transmit(buf);

        var received = intf2.getReceiverQueue().element();
        assertArrayEquals(buf, received);
    }

    @Test
    public void resolveAddress() throws UnknownHostException {
        var net = Network.create();
        var intf1 = EthernetInterface.create(sourceMac, net);

        var intf2 = EthernetInterface.create(destMac, net);
        var addr = InetAddress.getByName("2000:69a6:6030:939b::1");
        intf2.addInetAddress(addr);

        var mac = intf1.resolveInetAddress(addr);

        assertArrayEquals(intf2.getAddress(), mac);
    }
}
