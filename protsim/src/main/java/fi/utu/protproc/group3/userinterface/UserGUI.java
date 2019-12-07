package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.utils.SimulationReference;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class UserGUI extends Application {

    private Map<String, NetworkNode> nodes;
    private NetworkNode selectedNode;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        FXMLLoader peerLoader = new FXMLLoader(getClass().getResource("/fxml/peers.fxml"));
        Parent root = loader.load();
        peerLoader.load();

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
                Platform.runLater(() -> {
                    if (nodes.containsKey(s)) {
                        selectedNode = nodes.get(s);
                        if (selectedNode.nodeIsRunning()) {
                            actionBtn.setText("Shutdown");
                        } else {
                            actionBtn.setText("Start up");
                        }
                        Text routerLabel = (Text) root.lookup("#routerLabel");
                        routerLabel.setText(s);

                        Text nodeType = (Text) root.lookup("#nodeType");
                        if(selectedNode instanceof ClientNode) {
                            nodeType.setText("Client");
                        } else if(selectedNode instanceof ServerNode) {
                            nodeType.setText("Server");
                        } else if(selectedNode instanceof RouterNode) {
                            nodeType.setText("Router");
                        }

                        RowController rowController = loader.getController();
                        rowController.setRouter(selectedNode);
                        rowController.initialize(null, null);

                        if(selectedNode instanceof RouterNode) {
                            PeersController peersController = peerLoader.getController();
                            peersController.setRouter((RouterNode) selectedNode);
                            peersController.initialize(null, null);
                        }
                    }
                });
            }

            @Override
            public void buttonReleased(String s) {
            }
        });

        Thread thread = new Thread(() -> {
            while(true) {
                try {
                    pipe.blockingPump();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        actionBtn.setOnAction(actionEvent -> {
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

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.show();
    }
}
