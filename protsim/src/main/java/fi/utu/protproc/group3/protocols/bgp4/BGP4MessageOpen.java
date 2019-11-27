package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageOpen extends BGP4Message {
    byte BGP_VERSION = 0x04;

    static BGP4MessageOpen create(short myAutonomousSystem, short holdTime, int bgpIdentifier) {
        return new BGP4MessageOpenImpl((short) 29, BGP4Message.TYPE_OPEN, BGP_VERSION, myAutonomousSystem, holdTime, bgpIdentifier);
    }

    byte getVersion();
    short getMyAutonomousSystem();
    short getHoldTime();
    int getBGPIdentifier();
    byte getOptParmLen();

}
