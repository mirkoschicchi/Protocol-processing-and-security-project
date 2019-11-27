package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.*;

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
        short len;
        byte type;

        ByteBuffer buf = ByteBuffer.wrap(message);

        buf.get(marker);
        len = buf.getShort();
        type = buf.get();

        switch (type) {
            case BGP4Message.TYPE_OPEN:
                buf.get();
                short myAutonomousSystem = buf.getShort();
                short holdTime = buf.getShort();
                int bgpIdentifier = buf.getInt();
                return BGP4MessageOpen.create(myAutonomousSystem, holdTime, bgpIdentifier);

            case BGP4Message.TYPE_UPDATE:
                short withdrawnRoutesLength = buf.getShort();

                List<NetworkAddress> withdrawnRoutes = new ArrayList<NetworkAddress>();
                byte[] tmp = new byte[16];
                for (short i=0; i < withdrawnRoutesLength; i++) {
                    buf.get(tmp);
                    IPAddress addr = new IPAddress(tmp);
                    int prefLen = buf.getInt();
                    withdrawnRoutes.add(new NetworkAddress(addr, prefLen));
                }

                short totalPathAttributeLength = buf.getShort();

                List<NetworkAddress> networkLayerReachabilityInformation
                        = new ArrayList<NetworkAddress>();
                for (short i=0; i < totalPathAttributeLength; i++) {
                    var attrFlags = buf.get();
                    var attrType = buf.get();

                    var extended = (attrFlags & (1 << 4)) != 0;
                    int attrLen = extended ? buf.getShort() : buf.get();

                    var oldPos = buf.position();

                    switch (attrType) {
                        case 1: // ORIGIN
                        {
                            // TODO: Parse origin and set it on the update message
                        }
                        case 2: // AS_PATH
                        {
                            // TODO: Parse as path sequence and assign it
                        }
                        case 3: // NEXT_HOP
                        {
                            // TODO
                        }
                    }

                    buf.position(oldPos + attrLen);
                }

                return BGP4MessageUpdate.create(withdrawnRoutesLength,
                        withdrawnRoutes,
                        totalPathAttributeLength,
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
