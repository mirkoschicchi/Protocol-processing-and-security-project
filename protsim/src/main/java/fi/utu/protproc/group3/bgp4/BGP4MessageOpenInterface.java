package fi.utu.protproc.group3.bgp4;

public interface BGP4MessageOpenInterface extends BGP4MessageInterface {
    static BGP4MessageOpenInterface create(short version, int myAutonomousSystem, int holdTime, int bgpIdentifier) {
        throw new UnsupportedOperationException();
    }

    short getVersion();
    int getMyAutonomousSystem();
    int getHoldTime();
    int getBGPIdentifier();
}
