package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.OptionalParameter;

import java.util.List;

public interface BGP4MessageOpen extends BGP4Message {
    static BGP4MessageOpen create(short version, int myAutonomousSystem, int holdTime, int bgpIdentifier,
                                  short optParmLen, List<OptionalParameter> optionalParameters) {
        throw new UnsupportedOperationException();
    }

    short getVersion();
    int getMyAutonomousSystem();
    int getHoldTime();
    long getBGPIdentifier();
    short getOptParmLen();
    // TODO Remove the class
    List<OptionalParameter> getOptionalParameters();

}
