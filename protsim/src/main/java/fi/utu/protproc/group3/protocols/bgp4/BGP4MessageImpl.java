package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.ASPath;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BGP4MessageImpl implements BGP4Message {
    // Marker is set all to 1 following RFC-4271
    private final byte[] marker = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    private final short length;
    private final byte type;

    BGP4MessageImpl(short length, byte type) {
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
        buf.position(buf.position() + 2); // skip length
        byte type = buf.get();

        switch (type) {
            case BGP4Message.TYPE_OPEN:
                buf.position(buf.position() + 1); // skip version field
                short myAutonomousSystem = buf.getShort();
                short holdTime = buf.getShort();
                int bgpIdentifier = buf.getInt();
                return BGP4MessageOpen.create(myAutonomousSystem, holdTime, bgpIdentifier);

            case BGP4Message.TYPE_UPDATE:
                List<NetworkAddress> withdrawnRoutes = new ArrayList<>();
                List<NetworkAddress> networkLayerReachabilityInformation
                        = new ArrayList<>();
                byte[] tmp = new byte[16];

                // Skip withdrawnRoutesLength because it's 0 (withdrawnRoutes in MP_UNREACH_NLRI)
                buf.position(buf.position() + 2);

                // Skip Total Path Attribute Length, not needed
                buf.position(buf.position() + 2);

                // Path Attributes
                //  origin
                buf.position(buf.position() + 2); // skip attr type
                buf.position(buf.position() + 2); // skip attr length
                byte origin = buf.get();

                List<List<Short>> asPath = new ArrayList<>();
                buf.position(buf.position() + 2); // skip attr type
                short valueLen = buf.getShort();
                short cont = 0;
                while (cont < valueLen) {
                    List<Short> asSet = new ArrayList<>();
                    buf.position(buf.position() + 1); // skip as path type, always set to AS_SET
                    short asLen = buf.get();
                    cont += 2;
                    for (int i=0; i < asLen; i++) {
                        asSet.add(buf.getShort());
                        cont += 2;
                    }
                    asPath.add(asSet);
                }

                // MP_REACH_NLRI
                buf.position(buf.position() + 2); // skip attr type
                short byteLength = buf.getShort();
                buf.position(buf.position() + 3);  // skip AFI and SAFI definition
                byteLength -= 3;

                // nextHop
                buf.position(buf.position() + 1); // skip length of next hop, always 16
                buf.get(tmp);
                IPAddress nextHop = new IPAddress(tmp);
                byteLength = (short) (byteLength - (1 + nextHop.toArray().length));
                buf.get(); // skip reserved 0 byte
                byteLength--;

                while(byteLength > 0) {
                    NetworkAddress prefix = parseNetworkAddressesFromBuffer(buf);
                    networkLayerReachabilityInformation.add(prefix);
                    byteLength -= (1 + prefix.getRequiredBytesForPrefix());
                }

                // MP_UNREACH_NLRI
                buf.position(buf.position() + 2); // skip attr type
                buf.position(buf.position() + 2); // skip attr length
                buf.position(buf.position() + 3);  // skip AFI and SAFI definition
                while(buf.remaining() > 0) {
                    withdrawnRoutes.add(parseNetworkAddressesFromBuffer(buf));
                }

                return BGP4MessageUpdate.create(withdrawnRoutes, origin, new ASPath(asPath), nextHop,
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
