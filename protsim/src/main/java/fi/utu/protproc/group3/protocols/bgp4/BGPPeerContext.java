package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.finitestatemachine.FSMImpl;
import fi.utu.protproc.group3.finitestatemachine.InternalFSMCallbacksImpl;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.IPAddress;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;

public class BGPPeerContext {
    private final RouterNode router;
    private final EthernetInterface ethernetInterface;
    private final IPAddress peer;
    private final UntypedStateMachine fsm;
    private final BGPConnection connection;

    public BGPPeerContext(RouterNode router, EthernetInterface ethernetInterface, IPAddress peer) {
        this.router = router;
        this.ethernetInterface = ethernetInterface;
        this.peer = peer;
        this.connection = new BGPConnection(ethernetInterface, this);

        var isInitiator = ethernetInterface.getIpAddress().toArray()[15] < peer.toArray()[15];
        this.fsm = FSMImpl.newInstance(new InternalFSMCallbacksImpl() {
            @Override
            public void connectRemotePeer() {
                if (isInitiator) {
                    connection.connect(peer, BGPServer.PORT);
                }
            }

            @Override
            public void dropTCPConnection() {
                connection.close();
            }

            @Override
            public void closeBGPConnection() {
                connection.close();
            }
        });
    }

    public RouterNode getRouter() {
        return router;
    }

    public EthernetInterface getEthernetInterface() {
        return ethernetInterface;
    }

    public IPAddress getPeer() {
        return peer;
    }

    public UntypedStateMachine getFsm() {
        return fsm;
    }

    public void start() {
        this.fsm.fire(FSMImpl.FSMEvent.ManualStart);
    }

    public void stop() {
        this.fsm.fire(FSMImpl.FSMEvent.ManualStop);
    }

    public Connection getConnection() {
        return connection;
    }
}
