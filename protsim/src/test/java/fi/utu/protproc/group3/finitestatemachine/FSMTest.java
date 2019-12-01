package fi.utu.protproc.group3.finitestatemachine;

import org.junit.jupiter.api.Test;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;

public class FSMTest {

    @Test
    public void createFSM() {
        UntypedStateMachine fsm = FSMImpl.newInstance(new InternalFSMCallbacksImpl() {
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
        });

        fsm.fire(FSMImpl.FSMEvent.AutomaticStart);
        fsm.fire(FSMImpl.FSMEvent.BGPOpen_with_DelayOpenTimer_running);
        fsm.fire(FSMImpl.FSMEvent.ManualStop);
        fsm.fire(FSMImpl.FSMEvent.ManualStart);
        fsm.fire(FSMImpl.FSMEvent.DelayOpenTimer_Expires);
        fsm.fire(FSMImpl.FSMEvent.BGPOpen);
        fsm.fire(FSMImpl.FSMEvent.KeepAliveMsg);
        fsm.fire(FSMImpl.FSMEvent.ManualStop);
        fsm.fire(FSMImpl.FSMEvent.ManualStop);
    }
}
