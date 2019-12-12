package fi.utu.protproc.group3.protocols.bgp4.fsm;

import fi.utu.protproc.group3.utils.ASPath;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;

public class BGPCallbacksDefault implements BGPCallbacks {
    @Override
    public void connectRemotePeer() {

    }

    @Override
    public void releaseBGPResources() {

    }

    @Override
    public void completeBGPPeerInitialization() {

    }

    @Override
    public void dropTCPConnection() {

    }

    @Override
    public void sendOpenMessage() {

    }

    @Override
    public void sendKeepaliveMessage() {

    }

    @Override
    public void sendNotificationMessage(byte errorCode, byte subErrorCode, byte[] data) {

    }

    @Override
    public void sendUpdateMessage(List<NetworkAddress> withdrawnRoutes, byte origin,
                                  ASPath asPath, IPAddress nextHop,
                                  List<NetworkAddress> networkLayerReachabilityInformation) {

    }

    @Override
    public void performPeerOscillationDamping() {

    }

    @Override
    public void closeBGPConnection() {

    }

    @Override
    public void checkCollisionDetection() {

    }

    @Override
    public void deleteAllRoutes() {

    }

    @Override
    public void processUpdateMessage() {

    }

    @Override
    public void initializeBGPResources() {

    }

    @Override
    public void initiateTCPConnection() {

    }

    @Override
    public void listenForTCPConnection() {

    }

    @Override
    public void processTCPConnection() {

    }

    @Override
    public void rejectTCPConnection() {

    }
}
