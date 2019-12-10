package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.routing.TableRow;
import javafx.beans.property.*;

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
        this.ethernetInterfaceProperty = row.getInterface() != null ? new SimpleStringProperty(row.getInterface().print()) : new SimpleStringProperty("");
        this.asPathProperty = row.getAsPath() != null ? new SimpleStringProperty(row.getAsPath().toString()) : new SimpleStringProperty("");
    }

    public String getPrefixProperty() {
        return prefixProperty.get();
    }

    public StringProperty prefixProperty() {
        return prefixProperty;
    }

    public void setPrefixProperty(String prefixProperty) {
        this.prefixProperty.set(prefixProperty);
    }

    public String getNextHopProperty() {
        return nextHopProperty.get();
    }

    public StringProperty nextHopProperty() {
        return nextHopProperty;
    }

    public void setNextHopProperty(String nextHopProperty) {
        this.nextHopProperty.set(nextHopProperty);
    }

    public int getMetricProperty() {
        return (int) metricProperty.get();
    }

    public IntegerProperty metricProperty() {
        return metricProperty;
    }

    public void setMetricProperty(int metricProperty) {
        this.metricProperty.set(metricProperty);
    }

    public String getEthernetInterfaceProperty() {
        return ethernetInterfaceProperty.get();
    }

    public StringProperty ethernetInterfaceProperty() {
        return ethernetInterfaceProperty;
    }

    public void setEthernetInterfaceProperty(String ethernetInterfaceProperty) {
        this.ethernetInterfaceProperty.set(ethernetInterfaceProperty);
    }

    public String getAsPathProperty() {
        return asPathProperty.get();
    }

    public StringProperty asPathProperty() {
        return asPathProperty;
    }

    public void setAsPathProperty(String asPathProperty) {
        this.asPathProperty.set(asPathProperty);
    }
}
