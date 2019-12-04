package fi.utu.protproc.group3.protocols.bgp4.fsm;

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
    public void sendTrustRateMessage(int inheritTrust) {

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
