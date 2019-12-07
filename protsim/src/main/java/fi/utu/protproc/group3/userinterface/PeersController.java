package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.protocols.bgp4.BGPPeerContext;
import fi.utu.protproc.group3.utils.IPAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

public class PeersController implements Initializable {
    @FXML
    private TableView<PeersModel> table;

    @FXML
    private TableColumn<PeersModel, String> peerIPColumn;

    @FXML
    private TableColumn<PeersModel, String> connectionStatusColumn;

    private RouterNode router;
    private Map<IPAddress, BGPPeerContext> peers;
    private ArrayList<PeersModel> peersModels = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(router != null)  {
            peers = router.getPeerings();
            peers.forEach((key, value) ->
                peersModels.add(new PeersModel(key, value))
            );

            ObservableList<PeersModel> peerModelsList = FXCollections.observableArrayList(
                    peersModels
            );
            table.setItems(peerModelsList);

            peerIPColumn.setCellValueFactory(new PropertyValueFactory<>("peerIP"));
            connectionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("connectionStatus"));
        }

    }

    public RouterNode getRouter() {
        return router;
    }

    public void setRouter(RouterNode router) {
        this.router = router;
    }
}
