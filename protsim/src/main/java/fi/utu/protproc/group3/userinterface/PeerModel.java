package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PeerModel {
    private final BGPPeerContext context;
    private StringProperty peerProperty;
    private IntegerProperty bgpIdentifierProperty;
    private StringProperty statusProperty;

    public PeerModel(BGPPeerContext peerContext) {
        context = peerContext;
        statusProperty = new SimpleStringProperty(peerContext.getFsm().getCurrentState().toString());
        peerProperty = new SimpleStringProperty(peerContext.getPeer().toString());
        bgpIdentifierProperty = new SimpleIntegerProperty(peerContext.getBgpIdentifier());
    }

    public BGPPeerContext getContext() {
        return context;
    }

    public StringProperty peerProperty() {
        return peerProperty;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public IntegerProperty bgpIdentifierProperty() {
        return bgpIdentifierProperty;
    }
}
