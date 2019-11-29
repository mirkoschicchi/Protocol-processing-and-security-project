package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.nodes.NetworkNode;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Map;

public class UserInterfaceManager implements ViewerListener {
    private boolean loop = true;
    private Map<String, NetworkNode> nodes;
    private MultiGraph graph;
    private PopUpDemo menu;

    public UserInterfaceManager(@NotNull Viewer viewer, MultiGraph graph, Map<String, NetworkNode> nodes) {
        this.nodes = nodes;
        this.graph = graph;
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);

        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(this.graph);

        menu = new PopUpDemo();

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

            menu.toggleMenu(node);
        }
    }

    class PopUpDemo extends JPopupMenu {
        NetworkNode node;

        public PopUpDemo() {
            JMenuItem item;

            this.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            Border compound = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());
            this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), compound));

            this.add(item = new JMenuItem("Shutdown"));
            item.addActionListener(menuListener);

            this.add(item = new JMenuItem("Start"));
            item.addActionListener(menuListener);

            this.add(item = new JMenuItem("Routing table"));
            item.addActionListener(menuListener);

            this.add(item = new JMenuItem("Cancel"));
            item.addActionListener(menuListener);

            this.setVisible(false);
        }

        ActionListener menuListener = event -> {
            String invAction = event.getActionCommand();

            switch (invAction) {
                case "Shutdown":
                    if (node.nodeIsRunning()) {
                        node.shutdown();
                        graph.getNode(node.getHostname()).addAttribute("ui.style", "stroke-mode: plain; stroke-width: 3px; stroke-color: red;");
                        System.out.println("Node SHUTDOWN: " + node.getHostname());

                        this.setVisible(false);
                    }

                    break;
                case "Start":
                    if (!node.nodeIsRunning()) {
                        node.start();
                        graph.getNode(node.getHostname()).addAttribute("ui.style", "stroke-mode: none;");
                        System.out.println("Node START: " + node.getHostname());

                        this.setVisible(false);
                    }
                    break;
                case "Routing table":
                    System.out.println("Routing table TODO.");
                    break;
                case "Cancel":
                    System.out.println("Cancel was pressed.");
                    this.setVisible(false);
                    break;
                default:
                    System.out.println("You pressed something strange.");
            }
        };

        public void toggleMenu(NetworkNode node) {
            this.node = node;

            if (this.isVisible()) {
                this.setVisible(false);
            } else {
                this.show(null, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
                this.setVisible(true);
            }
        }
    }

    @Override
    public void buttonReleased(String s) { }
}