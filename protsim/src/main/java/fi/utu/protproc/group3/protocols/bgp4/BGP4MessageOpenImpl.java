package fi.utu.protproc.group3.protocols.bgp4;

import java.nio.ByteBuffer;

public class BGP4MessageOpenImpl extends BGP4MessageImpl implements BGP4MessageOpen {
    private byte version;
    private short myAutonomousSystem;
    private short holdTime;
    private int bgpIdentifier;

    public BGP4MessageOpenImpl(short length, byte type, byte version, short myAutonomousSystem, short holdTime, int bgpIdentifier) {
        super(length, type);
        this.version = version;
        this.myAutonomousSystem = myAutonomousSystem;
        this.holdTime = holdTime;
        this.bgpIdentifier = bgpIdentifier;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public short getMyAutonomousSystem() {
        return myAutonomousSystem;
    }

    @Override
    public short getHoldTime() {
        return holdTime;
    }

    @Override
    public int getBGPIdentifier() {
        return bgpIdentifier;
    }

    @Override
    public byte getOptParmLen() {
        return (byte) 0x00;
    }

    @Override
    public byte[] serialize() {
        byte[] serialized;
        serialized = ByteBuffer.allocate(29)
                .put(super.serialize())
                .put(getVersion())
                .putShort(getMyAutonomousSystem())
                .putShort(getHoldTime())
                .putInt(getBGPIdentifier())
                .put(getOptParmLen()).array();

        return serialized;
    }
}
