package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.RouterNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PeersController implements Initializable {
    @FXML
    private TableView<PeerModel> table;

    @FXML
    private TableColumn<PeerModel, String> peerColumn;

    @FXML
    private TableColumn<PeerModel, Number> identifierColumn;

    @FXML
    private TableColumn<PeerModel, String> statusColumn;

    private ObjectProperty<RouterNode> router = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        peerColumn.setCellValueFactory(new PropertyValueFactory<>("peer"));
        identifierColumn.setCellValueFactory(new PropertyValueFactory<>("bgpIdentifier"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        router.addListener((obs, oldValue, newValue) -> {
            if (router.get() != null) {
                table.setItems(FXCollections.observableList(newValue.getPeerings().stream().map(PeerModel::new).collect(Collectors.toList())));
            } else {
                table.getItems().clear();
            }
        });
    }

    public ObjectProperty<RouterNode> routerProperty() {
        return router;
    }
}
