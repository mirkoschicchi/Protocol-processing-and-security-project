package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.utils.SimulationReference;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class NodeController implements Initializable {
    @FXML
    private PeersController peersController;
    @FXML
    private RoutingTableController routesController;

    @FXML
    private Button actionBtn;
    @FXML
    private Text nodeType;
    @FXML
    private Text nodeName;

    private ObjectProperty<NetworkNode> node = new SimpleObjectProperty<>();

    public ObjectProperty<NetworkNode> nodeProperty() {
        return node;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        routesController.nodeProperty().bind(nodeProperty());

        node.addListener((obs, oldValue, newValue) -> {
            var router = newValue instanceof RouterNode ? ((RouterNode) newValue) : null;
            peersController.routerProperty().set(router);

            nodeName.setText(newValue != null ? newValue.getHostname() : "");

            if (newValue.nodeIsRunning()) {
                actionBtn.setText("Shutdown");
            } else {
                actionBtn.setText("Start up");
            }

            if (newValue instanceof ClientNode) {
                nodeType.setText("Client");
            } else if (newValue instanceof ServerNode) {
                nodeType.setText("Server");
            } else if (newValue instanceof RouterNode) {
                nodeType.setText("Router");
            } else {
                nodeType.setText("");
            }
        });

        actionBtn.addEventHandler(ActionEvent.ACTION, evt -> {
            var node = this.node.get();
            if (node != null) {
                var gn = SimulationReference.simulation.getGraph().getNode(node.getHostname());
                if (node.nodeIsRunning()) {
                    node.shutdown();
                    gn.addAttribute("ui.style", "stroke-mode: plain; stroke-width: 3px; stroke-color: red;");
                    actionBtn.setText("Start up");
                } else {
                    node.start();
                    gn.addAttribute("ui.style", "stroke-mode: none;");
                    actionBtn.setText("Shutdown");
                }
            }
        });
    }
}
