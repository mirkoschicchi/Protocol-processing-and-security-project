package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.simulator.Network;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private ObjectProperty<Object> selectedNode = new SimpleObjectProperty<>();
    private ObjectProperty<NetworkNode> selectedHost = new SimpleObjectProperty<>();
    private ObjectProperty<Network> selectedNetwork = new SimpleObjectProperty<>();

    @FXML
    private NodeController nodeController;

    @FXML
    private NetworkController networkController;

    @FXML
    private TabPane nodeTypeTabs;

    @FXML
    private Tab nodeTab;

    @FXML
    private Tab networkTab;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectedNode.addListener((obs, oldValue, newValue) -> {
            if (newValue instanceof NetworkNode) {
                selectedHost.set((NetworkNode) newValue);
                nodeTypeTabs.getSelectionModel().select(nodeTab);
            } else {
                selectedHost.set(null);
            }

            if (newValue instanceof Network) {
                selectedNetwork.set((Network) newValue);
                nodeTypeTabs.getSelectionModel().select(networkTab);
            } else {
                selectedNetwork.set(null);
            }
        });

        nodeController.nodeProperty().bind(selectedHost);
        networkController.selectedNetworkProperty().bind(selectedNetwork);
    }

    public ObjectProperty<Object> selectedNodeProperty() {
        return selectedNode;
    }
}
