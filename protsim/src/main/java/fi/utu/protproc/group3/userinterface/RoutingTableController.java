package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
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

public class RoutingTableController implements Initializable {
    private ObjectProperty<NetworkNode> node = new SimpleObjectProperty<>();

    @FXML
    private TableView<RouteModel> table;

    @FXML
    private TableColumn<RouteModel, String> prefixColumn;

    @FXML
    private TableColumn<RouteModel, String> nextHopColumn;

    @FXML
    private TableColumn<RouteModel, Integer> metricColumn;

    @FXML
    private TableColumn<RouteModel, String> ethernetInterfaceColumn;

    @FXML
    private TableColumn<RouteModel, String> asPathColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        prefixColumn.setCellValueFactory(new PropertyValueFactory<>("prefix"));
        nextHopColumn.setCellValueFactory(new PropertyValueFactory<>("nextHop"));
        metricColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
        ethernetInterfaceColumn.setCellValueFactory(new PropertyValueFactory<>("ethernetInterface"));
        asPathColumn.setCellValueFactory(new PropertyValueFactory<>("asPath"));

        node.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                table.setItems(FXCollections.observableList(newValue.getRoutingTable().getRows().stream().map(RouteModel::new).collect(Collectors.toUnmodifiableList())));
            } else {
                table.getItems().clear();
            }
        });
    }

    public ObjectProperty<NetworkNode> nodeProperty() {
        return node;
    }

    public void setNode(NetworkNode node) {
        this.node.set(node);
    }
}
