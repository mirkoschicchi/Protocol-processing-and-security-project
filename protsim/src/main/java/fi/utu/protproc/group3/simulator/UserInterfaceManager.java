package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.nodes.NetworkNode;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class UserInterfaceManager implements ViewerListener {
    private boolean loop = true;
    private Map<String, NetworkNode> nodes;

    public UserInterfaceManager(@NotNull Viewer viewer, MultiGraph graph, Map<String, NetworkNode> nodes) {
        this.nodes = nodes;
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);

        while(loop) {
            fromViewer.pump();
        }
    }

    @Override
    public void viewClosed(String s) {
        loop = false;
    }

    @Override
    public void buttonPushed(String s) {
        if (nodes.containsKey(s)) {
            var node = nodes.get(s);
            if (node.nodeIsRunning()) {
                node.shutdown();
                System.out.println("Node SHUTDOWN: " + s);
            } else {
                node.start();
                System.out.println("Node START: " + s);
            }
        }
    }

    @Override
    public void buttonReleased(String s) { }
}