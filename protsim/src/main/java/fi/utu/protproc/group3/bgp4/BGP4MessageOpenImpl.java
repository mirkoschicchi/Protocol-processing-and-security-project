package fi.utu.protproc.group3.bgp4;

public interface BGP4MessageOpenImpl extends BGP4MessageImpl {
    static BGP4MessageOpenImpl create(short version, int myAutonomousSystem, int holdTime, int bgpIdentifier) {
        throw new UnsupportedOperationException();
    }

    short getVersion();
    int getMyAutonomousSystem();
    int getHoldTime();
    int getBGPIdentifier();
}
