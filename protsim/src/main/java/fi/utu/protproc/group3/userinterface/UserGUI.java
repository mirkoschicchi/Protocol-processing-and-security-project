package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.simulator.UserInterfaceManager;
import fi.utu.protproc.group3.utils.SimulationReference;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import javafx.embed.swing.SwingNode;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class UserGUI extends Application {

    private Map<String, NetworkNode> nodes;
    private RouterNode selectedNode;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        nodes = new HashMap<>();
        nodes = SimulationReference.nodes;

        Viewer viewer = SimulationReference.simulation.getViewer();
        SwingNode node = (SwingNode) root.lookup("#swingnode");
        View view = viewer.getDefaultView();

        Button actionBtn = (Button) root.lookup("#actionBtn");

        ViewerPipe pipe = viewer.newViewerPipe();
        pipe.addAttributeSink(viewer.getGraphicGraph());

        pipe.addViewerListener(new ViewerListener() {
            @Override
            public void viewClosed(String s) {

            }

            @Override
            public void buttonPushed(String s) {
                if (nodes.containsKey(s)) {
                    selectedNode = (RouterNode) nodes.get(s);
                    if(selectedNode.nodeIsRunning()) {
                        actionBtn.setText("Shutdown");
                    } else {
                        actionBtn.setText("Start up");
                    }
                    Text routerLabel = (Text) root.lookup("#routerLabel");
                    routerLabel.setText(s);
                    System.out.println(selectedNode.getHostname());
                    RowController rowController = loader.getController();
                    rowController.setRouter(selectedNode);
                    rowController.initialize(null, null);
                }
            }

            @Override
            public void buttonReleased(String s) {

            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    pipe.pump();
                }

            }
        });

        actionBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (selectedNode != null && selectedNode.nodeIsRunning()) {
                    selectedNode.shutdown();
                    SimulationReference.simulation.getGraph().getNode(selectedNode.getHostname()).addAttribute("ui.style", "stroke-mode: plain; stroke-width: 3px; stroke-color: red;");
                    System.out.println("Node SHUTDOWN: " + selectedNode.getHostname());
                    actionBtn.setText("Start up");
                } else if(selectedNode != null && !selectedNode.nodeIsRunning()) {
                    selectedNode.start();
                    SimulationReference.simulation.getGraph().getNode(selectedNode.getHostname()).addAttribute("ui.style", "stroke-mode: none;");
                    System.out.println("Node START: " + selectedNode.getHostname());
                    actionBtn.setText("Shutdown");
                }
            }
        });
        thread.start();

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

}
