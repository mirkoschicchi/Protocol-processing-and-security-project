package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NodeController implements Initializable {
    public PeersController peersController;
    public RoutingTableController routesController;

    public AnchorPane self;
    public Button actionBtn;
    public Text nodeType;
    public Text nodeName;
    public Hyperlink ipAddress;

    private ObjectProperty<NetworkNode> node = new SimpleObjectProperty<>();

    public ObjectProperty<NetworkNode> nodeProperty() {
        return node;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        routesController.nodeProperty().bind(nodeProperty());

        node.addListener((obs, oldValue, newValue) -> {
            actionBtn.setDisable(newValue == null);
            ipAddress.setDisable(newValue == null);
            if (newValue != null) {
                var router = newValue instanceof RouterNode ? ((RouterNode) newValue) : null;
                peersController.routerProperty().set(router);

                nodeName.setText(newValue != null ? newValue.getHostname() : "");

                if (newValue.isOnline()) {
                    actionBtn.setText("Shutdown");
                } else {
                    actionBtn.setText("Start up");
                }

                nodeType.setText(NetworkNode.getNodeTypeName(newValue));

                if (newValue.getInterfaces().size() == 1) {
                    String ipAddr = newValue.getInterfaces().iterator().next().getIpAddress().toString();
                    ipAddress.setText(ipAddr);
                } else {
                    ipAddress.setText("[multiple]");
                }
            } else {
                nodeType.setText("");
                nodeName.setText("");
                peersController.routerProperty().set(null);
                ipAddress.setText("");
            }
        });

        actionBtn.addEventHandler(ActionEvent.ACTION, evt -> {
            var node = this.node.get();
            if (node != null) {
                var gn = SimulationReference.simulation.getGraph().getNode(node.getHostname());
                if (node.isOnline()) {
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

        ipAddress.addEventHandler(ActionEvent.ACTION, evt -> {
            var node = this.node.get();
            var content = new ClipboardContent();
            content.putString(node.getInterfaces().stream().map(i -> i.getIpAddress().toString()).collect(Collectors.joining("\n")));
            Clipboard.getSystemClipboard().setContent(content);
        });
    }
}
