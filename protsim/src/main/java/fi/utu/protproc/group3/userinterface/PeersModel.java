package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.utils.IPAddress;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PeersModel {
    private StringProperty peerIPProperty;
    private StringProperty connectionStatusProperty;

    public PeersModel(IPAddress peerIPAddress, BGPPeerContext peerContext) {
        this.peerIPProperty = new SimpleStringProperty(peerIPAddress.toString());
        this.connectionStatusProperty = new SimpleStringProperty(peerContext.getFsm().getStatus().toString());
    }

    public String getpeerIPProperty() {
        return peerIPProperty.get();
    }

    public StringProperty peerIPProperty() {
        return peerIPProperty;
    }

    public void setPeerNameProperty(String peerIPProperty) {
        this.peerIPProperty.set(peerIPProperty);
    }

    public String getConnectionStatusProperty() {
        return connectionStatusProperty.get();
    }

    public StringProperty connectionStatusProperty() {
        return connectionStatusProperty;
    }

    public void setConnectionStatusProperty(String connectionStatusProperty) {
        this.connectionStatusProperty.set(connectionStatusProperty);
    }
}
