package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.OptionalParameter;

import java.util.List;

public interface BGP4MessageOpenImpl extends BGP4MessageImpl {
    static BGP4MessageOpenImpl create(short version, int myAutonomousSystem, int holdTime, int bgpIdentifier,
                                      short optParmLen, List<OptionalParameter> optionalParameters) {
        throw new UnsupportedOperationException();
    }

    short getVersion();
    int getMyAutonomousSystem();
    int getHoldTime();
    long getBGPIdentifier();
    short getOptParmLen();
    List<OptionalParameter> getOptionalParameters();

}
