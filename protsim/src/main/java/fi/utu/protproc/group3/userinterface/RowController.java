package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.routing.TableRow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.*;

public class RowController implements Initializable {
    @FXML
    private TableView<RoutingTableModel> table;

    @FXML
    private TableColumn<RoutingTableModel, String> prefixColumn;

    @FXML
    private TableColumn<RoutingTableModel, String> nextHopColumn;

    @FXML
    private TableColumn<RoutingTableModel, Integer> metricColumn;

    @FXML
    private TableColumn<RoutingTableModel, String> ethernetInterfaceColumn;

    @FXML
    private TableColumn<RoutingTableModel, String> asPathColumn;

    private RouterNode router;

    RoutingTable routingTable;
    Collection<TableRow> rows;
    ArrayList<RoutingTableModel> rowModels = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            routingTable = router.getRoutingTable();
            rows = routingTable.getRows();
            for(TableRow row: rows) {
                rowModels.add(new RoutingTableModel(row));
            }

            ObservableList<RoutingTableModel> rowModelsList = FXCollections.observableArrayList(
                    rowModels
            );
            table.setItems(rowModelsList);

            prefixColumn.setCellValueFactory(new PropertyValueFactory<>("prefix"));
            nextHopColumn.setCellValueFactory(new PropertyValueFactory<>("nextHop"));
            metricColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
            ethernetInterfaceColumn.setCellValueFactory(new PropertyValueFactory<>("ethernetInterface"));
            asPathColumn.setCellValueFactory(new PropertyValueFactory<>("asPath"));
        });


    }

    public RouterNode getRouter() {
        return router;
    }

    public void setRouter(RouterNode router) {
        this.router = router;
    }
}
