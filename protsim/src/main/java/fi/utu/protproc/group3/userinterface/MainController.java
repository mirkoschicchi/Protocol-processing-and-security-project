package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private ObjectProperty<NetworkNode> selectedNode = new SimpleObjectProperty<>();

    @FXML
    private NodeController nodeController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nodeController.nodeProperty().bind(selectedNode);
    }

    public ObjectProperty<NetworkNode> selectedNodeProperty() {
        return selectedNode;
    }
}
