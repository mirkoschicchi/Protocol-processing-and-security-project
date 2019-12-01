package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class BGP4MessageImpl implements BGP4Message {
    // Marker is set all to 1 following RFC-4271
    private byte[] marker = {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
    private short length;
    private byte type;

    public BGP4MessageImpl(short length, byte type) {
        this.length = length;
        this.type = type;
    }

    private static void getNetworkAddressesList(ByteBuffer buf, List<NetworkAddress> netAddrList, int elemNum) {
        int prefLen;
        IPAddress addr;
        for (short i = 0; i < elemNum; i++) {
            byte[] app = new byte[16];
            prefLen = buf.get();
            buf.get(app);
            addr = new IPAddress(app);
            netAddrList.add(new NetworkAddress(addr, prefLen));
        }
    }

    @Override
    public byte[] getMarker() {
        return marker;
    }

    @Override
    public short getLength() {
        return length;
    }

    @Override
    public byte getType() {
        return type;
    }

    @Override
    public byte[] serialize() {
        byte[] serialized;
        serialized = ByteBuffer.allocate(19)
                .put(getMarker())
                .putShort(getLength())
                .put(getType()).array();

        return serialized;
    }

    public static BGP4Message parse(byte[] message) {
        byte[] marker = new byte[16];

        ByteBuffer buf = ByteBuffer.wrap(message);

        buf.get(marker);
        short len = buf.getShort();
        byte type = buf.get();

        switch (type) {
            case BGP4Message.TYPE_OPEN:
                buf.get();
                short myAutonomousSystem = buf.getShort();
                short holdTime = buf.getShort();
                int bgpIdentifier = buf.getInt();
                return BGP4MessageOpen.create(myAutonomousSystem, holdTime, bgpIdentifier);

            case BGP4Message.TYPE_UPDATE:
                List<NetworkAddress> withdrawnRoutes = new ArrayList<NetworkAddress>();
                byte[] tmp = new byte[16];
                IPAddress addr;
                int prefLen = 0;

                //  withdrawnRoutes
                short withdrawnRoutesLength = buf.getShort();
                getNetworkAddressesList(buf, withdrawnRoutes, withdrawnRoutesLength / 17);

                // Total Path Attribute Length
                buf.getShort();

                //  origin
                buf.getInt();
                byte origin = buf.get();

                List<List<Short>> asPath = new ArrayList<>();
                buf.getShort();
                short valueLen = buf.getShort();
                short cont = 0;
                while (cont < valueLen) {
                    List<Short> asSet = new ArrayList<>();
                    buf.get();
                    short asLen = buf.get();
                    cont += 2;
                    for (int i=0; i < asLen; i++) {
                        asSet.add(buf.getShort());
                        cont += 2;
                    }
                    asPath.add(asSet);
                }

                // nextHop
                buf.getInt();
                prefLen = buf.get();
                buf.get(tmp);
                addr = new IPAddress(tmp);
                NetworkAddress nextHop = new NetworkAddress(addr, prefLen);

                List<NetworkAddress> networkLayerReachabilityInformation
                        = new ArrayList<NetworkAddress>();
                //int nlriLen = buf.capacity() - 23 - 5 - 4 - valueLen - 21;
                getNetworkAddressesList(buf, networkLayerReachabilityInformation, buf.remaining() / 17);

                return BGP4MessageUpdate.create(withdrawnRoutes, origin, asPath, nextHop.getAddress(),
                        networkLayerReachabilityInformation);

            case BGP4Message.TYPE_NOTIFICATION:
                byte errorCode = buf.get();
                byte errorSubCode = buf.get();
                byte[] data = new byte[buf.remaining()];
                buf.get(data, 0, buf.remaining());
                return BGP4MessageNotification.create(errorCode, errorSubCode, data);

            case BGP4Message.TYPE_KEEPALIVE:
                return BGP4MessageKeepalive.create();

            default:
                throw new UnsupportedOperationException();
        }
    }
}
