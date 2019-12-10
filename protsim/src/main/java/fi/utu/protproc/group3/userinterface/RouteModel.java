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
        this.ethernetInterfaceProperty = row.getInterface() != null ? new SimpleStringProperty(row.getInterface().toString()) : new SimpleStringProperty("");
        this.asPathProperty = row.getAsPath() != null ? new SimpleStringProperty(formatAsPath(row.getAsPath())) : new SimpleStringProperty("");
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

    private String formatAsPath(List<List<Short>> path) {
        if (path == null || path.size() < 2) return "";

        var as = path.get(0);
        var idents = path.get(1);

        var result = new StringBuilder();
        for (var i = 0; i < as.size(); i++) {
            if (result.length() > 0) result.append(',');
            result.append("AS").append(as.get(i)).append('@').append(idents.get(i));
        }
        return result.toString();
    }
}
