package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.routing.TableRow;
import javafx.beans.property.*;

import java.util.List;

public class RouteModel {
    private StringProperty prefixProperty;
    private StringProperty nextHopProperty;
    private IntegerProperty metricProperty;
    private StringProperty ethernetInterfaceProperty;
    private StringProperty asPathProperty;

    public RouteModel(TableRow row) {
        this.prefixProperty = row.getPrefix() != null ? new SimpleStringProperty(row.getPrefix().toString()) : new SimpleStringProperty("");
        this.nextHopProperty = row.getNextHop() != null ? new SimpleStringProperty(row.getNextHop().toString()) : new SimpleStringProperty("");
        this.metricProperty = new SimpleIntegerProperty((int) row.getCalculatedMetric());
        this.ethernetInterfaceProperty = row.getInterface() != null ? new SimpleStringProperty(row.getInterface().getNetwork().getNetworkName()) : new SimpleStringProperty("");
        this.asPathProperty = row.getAsPath() != null ? new SimpleStringProperty(row.getAsPath().toString()) : new SimpleStringProperty("");
    }

    public StringProperty prefixProperty() {
        return prefixProperty;
    }

    public StringProperty nextHopProperty() {
        return nextHopProperty;
    }

    public IntegerProperty metricProperty() {
        return metricProperty;
    }

    public StringProperty ethernetInterfaceProperty() {
        return ethernetInterfaceProperty;
    }

    public StringProperty asPathProperty() {
        return asPathProperty;
    }
}
