package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.simulator.Simulation;
import fi.utu.protproc.group3.utils.SimulationReference;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.util.Collection;
import javafx.embed.swing.SwingNode;

import javax.swing.*;

public class UserGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Viewer viewer = SimulationReference.simulation.getViewer();
        SwingNode node = (SwingNode) root.lookup("#swingnode");
        View view = viewer.getDefaultView();

        AnchorPane.setTopAnchor(node, 0d);
        AnchorPane.setBottomAnchor(node, 0d);
        AnchorPane.setLeftAnchor(node, 0d);
        AnchorPane.setRightAnchor(node, 0d);

        node.setContent((JComponent) view);
        Scene scene = new Scene(root, 900, 900);

        primaryStage.setTitle("BGP simulator");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

   public void showRoutingTable(RouterNode router) {
        Collection<TableRow> rows = router.getRoutingTable().getRows();
        for(TableRow row: rows) {
            StringProperty destNetworkAddress = new SimpleStringProperty(row.getPrefix().toString());
            StringProperty nextHop = new SimpleStringProperty(row.getNextHop().toString());
            DoubleProperty metric = new SimpleDoubleProperty(row.getCalculatedMetric());
            StringProperty ethernetInterface = new SimpleStringProperty(row.getEInterface().toString());
        }
    }



   /* public static void main(String[] args) {
        // Here you can work with args - command line parameters
        launch();
    }*/
}
