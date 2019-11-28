package fi.utu.protproc.group3.simulator;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

public class UserInterfaceManager implements ViewerListener {
    private boolean loop = true;

    public UserInterfaceManager(Viewer viewer, MultiGraph graph) {
        // The default action when closing the view is to quit
        // the program.
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        // We connect back the viewer to the graph,
        // the graph becomes a sink for the viewer.
        // We also install us as a viewer listener to
        // intercept the graphic events.
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);

        // Then we need a loop to wait for events.
        // In this loop we will need to call the
        // pump() method to copy back events that have
        // already occurred in the viewer thread inside
        // our thread.

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
        System.out.println("Button pushed on node " + s);
    }

    @Override
    public void buttonReleased(String s) {
        System.out.println("Button released on node " + s);
    }
}