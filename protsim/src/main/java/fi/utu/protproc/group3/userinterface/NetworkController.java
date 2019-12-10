package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.simulator.Network;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class NetworkController implements Initializable {
    private ObjectProperty<Network> selectedNetwork = new SimpleObjectProperty<>();

    public Button actionBtn;
    public AnchorPane self;
    public Text networkAddress;
    public Text networkName;
    public TableView devices;
    public TableColumn deviceIpColumn;
    public TableColumn deviceHostnameColumn;
    public TableColumn deviceTypeColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        deviceIpColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        deviceHostnameColumn.setCellValueFactory(new PropertyValueFactory<>("hostname"));
        deviceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        devices.prefWidthProperty().bind(self.prefWidthProperty());

        selectedNetwork.addListener((obs, oldValue, newValue) -> {
            actionBtn.setDisable(newValue == null);
            if (newValue != null) {
                networkName.setText(newValue.getNetworkName());
                networkAddress.setText(newValue.getNetworkAddress().toString());

                devices.getItems().clear();
                for (var device : newValue.getDevices()) {
                    devices.getItems().add(new NetworkDeviceModel(device));
                }
            } else {
                networkName.setText("");
                networkAddress.setText("");
                devices.getItems().clear();
            }
        });
    }

    public ObjectProperty<Network> selectedNetworkProperty() {
        return selectedNetwork;
    }

    public static class NetworkDeviceModel {
        private final StringProperty ip;
        private final StringProperty hostname;
        private final StringProperty type;

        public NetworkDeviceModel(EthernetInterface intf) {
            ip = new SimpleStringProperty(intf.getIpAddress().toString());
            hostname = new SimpleStringProperty(intf.getHost().getHostname());
            type = new SimpleStringProperty(NetworkNode.getNodeTypeName(intf.getHost()));
        }

        public StringProperty ipProperty() {
            return ip;
        }

        public StringProperty hostnameProperty() {
            return hostname;
        }

        public StringProperty typeProperty() {
            return type;
        }
    }
}
