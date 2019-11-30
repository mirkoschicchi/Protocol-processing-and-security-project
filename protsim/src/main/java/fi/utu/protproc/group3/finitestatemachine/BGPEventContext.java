package fi.utu.protproc.group3.finitestatemachine;

import fi.utu.protproc.group3.utils.IPAddress;

public class BGPEventContext {
    private final IPAddress peer;

    public BGPEventContext(IPAddress peer) {
        this.peer = peer;
    }
}
