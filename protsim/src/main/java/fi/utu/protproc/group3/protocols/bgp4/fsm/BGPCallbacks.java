package fi.utu.protproc.group3.protocols.bgp4.fsm;

import fi.utu.protproc.group3.utils.ASPath;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;

interface BGPCallbacks {

    /**
     * Instantiate a connection to a remote peer
     */
    void connectRemotePeer();

    /**
     * Release all BGP resources
     */
    void releaseBGPResources();

    void completeBGPPeerInitialization();

    void dropTCPConnection();

    void sendOpenMessage();

    void sendKeepaliveMessage();

    void sendNotificationMessage(byte errorCode, byte subErrorCode, byte[] data);

    void sendUpdateMessage(List<NetworkAddress> withdrawnRoutes, byte origin,
                           ASPath asPath, IPAddress nextHop,
                           List<NetworkAddress> networkLayerReachabilityInformation);

    void performPeerOscillationDamping();

    void closeBGPConnection();

    void checkCollisionDetection();

    void deleteAllRoutes();

    void processUpdateMessage();

    void initializeBGPResources();

    void initiateTCPConnection();

    void listenForTCPConnection();

    void processTCPConnection();

    void rejectTCPConnection();
}
