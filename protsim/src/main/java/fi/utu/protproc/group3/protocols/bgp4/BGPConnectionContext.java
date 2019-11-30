package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.configuration.RouterConfiguration;
import fi.utu.protproc.group3.finitestatemachine.FSMImpl;
import fi.utu.protproc.group3.finitestatemachine.InternalFSMCallbacksImpl;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.RouterNodeImpl;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;

public class BGPConnectionContext {
    UntypedStateMachine fsm = FSMImpl.newInstance(new InternalFSMCallbacksImpl());
    RouterNode routerNode;

    public BGPConnectionContext(SimulationBuilderContext context, RouterConfiguration configuration) {
        this.routerNode = new RouterNodeImpl(context, configuration);
    }


}
