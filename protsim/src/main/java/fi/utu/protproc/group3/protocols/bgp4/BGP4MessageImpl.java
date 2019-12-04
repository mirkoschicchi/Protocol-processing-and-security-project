package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static NetworkAddress parseNetworkAddressesFromBuffer(ByteBuffer buf) {
        int prefLen;
        IPAddress addr;
        prefLen = buf.get();
        int byteNum = (prefLen % 8 > 0) ? prefLen / 8 + 1 : prefLen / 8;
        byte[] app = new byte[byteNum];
        buf.get(app);
        byte[] wrapper = new byte[16];
        Arrays.fill(wrapper, (byte) 0x00);
        System.arraycopy(app, 0, wrapper, 0, app.length);
        addr = new IPAddress(wrapper);
        return new NetworkAddress(addr, prefLen);
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
                while(withdrawnRoutesLength > 0) {
                    withdrawnRoutes.add(parseNetworkAddressesFromBuffer(buf));
                    withdrawnRoutesLength -= 1 + withdrawnRoutes.get(withdrawnRoutes.size() -1).getRequiredBytesForPrefix();
                }

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
                buf.get(tmp);
                IPAddress nextHop = new IPAddress(tmp);

                List<NetworkAddress> networkLayerReachabilityInformation
                        = new ArrayList<NetworkAddress>();
                while(buf.remaining() > 0) {
                    networkLayerReachabilityInformation.add(parseNetworkAddressesFromBuffer(buf));
                }

                return BGP4MessageUpdate.create(withdrawnRoutes, origin, asPath, nextHop,
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
