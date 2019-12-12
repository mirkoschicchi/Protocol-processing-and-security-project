package fi.utu.protproc.group3.userinterface;

import fi.utu.protproc.group3.graph.GraphAttributes;
import fi.utu.protproc.group3.nodes.NetworkNode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Viewer viewer = SimulationReference.simulation.getViewer();
        SwingNode node = (SwingNode) root.lookup("#swingnode");
        View view = viewer.getDefaultView();

        MainController controller = loader.getController();

        ViewerPipe pipe = viewer.newViewerPipe();
        pipe.addAttributeSink(viewer.getGraphicGraph());

        pipe.addViewerListener(new ViewerListener() {
            @Override
            public void viewClosed(String s) {
            }

            @Override
            public void buttonPushed(String s) {
                var gn = SimulationReference.simulation.getGraph().getNode(s);
                if (gn.hasAttribute(GraphAttributes.OBJECT)) {
                    var obj = gn.getAttribute(GraphAttributes.OBJECT);
                    Platform.runLater(() -> controller.selectedNodeProperty().set(obj));
                }
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
        thread.start();

        AnchorPane.setTopAnchor(node, 0d);
        AnchorPane.setBottomAnchor(node, 0d);
        AnchorPane.setLeftAnchor(node, 0d);
        AnchorPane.setRightAnchor(node, 0d);

        node.setContent((JComponent) view);
        Scene scene = new Scene(root, 900, 900);
        primaryStage.setTitle("BGP simulator");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        scene.setUserData(controller);

        primaryStage.show();
    }
}
