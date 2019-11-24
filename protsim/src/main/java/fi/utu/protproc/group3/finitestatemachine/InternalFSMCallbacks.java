package fi.utu.protproc.group3.finitestatemachine;

public interface InternalFSMCallbacks {

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

    void sendNotificationMessage(byte errorCode);

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
