package fi.utu.protproc.group3;

import fi.utu.protproc.group3.simulator.Simulation;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

public class App {
    public static void main(String[] args) throws Exception {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        var sim = Simulation.create();

        Graph graph = new MultiGraph("Protocol Processing Project");
        graph.setAttribute( "ui.antialias" );
        String path = System.getProperty("user.dir") + "/src/main/java/fi/utu/protproc/group3/images/";
        String styleSheet =
                "graph {"+
                        "fill-mode: plain;"+
                        "fill-color: white, gray;"+
                        "padding: 60px;"+
                        "}"+
                        "node {"+
                        "shape: box;"+
                        "fill-mode: image-scaled;"+
                        "}"+
                        "node.isp {"+
                        "size: 100px;"+
                        "fill-image: url('" + path + "kisspng-internet-cloud.png" + "');" +
                        "}"+
                        "node.routers {"+
                        "size: 60px;"+
                        "fill-image: url('" + path + "kisspng-wireless-router.png" + "');" +
                        "}"+
                        "node.localNets {"+
                        "size: 60px;"+
                        "fill-image: url('" + path + "kisspng-intranet.png" + "');" +
                        "}"+
                        "node.clients {"+
                        "size: 60px;"+
                        "fill-image: url('" + path + "kisspng-computer.png" + "');" +
                        "}"+
                        "node.servers {"+
                        "size: 40px, 60px;"+
                        "fill-image: url('" + path + "kisspng-server.png" + "');" +
                        "}"+
                        "edge {"+
                        "shape: cubic-curve;"+
                        "arrow-shape: none;"+
                        "size: 3px;"+
                        "}";
        graph.addAttribute("ui.stylesheet", styleSheet);

        var ispLink = sim.createNetwork();
        graph.addNode("ispLink").addAttribute("ui.class", "isp");
        Node ispLinkNode = graph.getNode("ispLink");
        ispLinkNode.setAttribute("xyz", 0, 0, 0);

        var localNet1 = sim.createNetwork();
        graph.addNode("localNet1").addAttribute("ui.class", "localNets");
        Node localNet1Node = graph.getNode("localNet1");
        localNet1Node.setAttribute("xyz", -2, 0, 0);

        sim.createRouter(localNet1, ispLink);
        graph.addNode("router1").addAttribute("ui.class", "routers");
        Node router1Node = graph.getNode("router1");
        router1Node.setAttribute("xyz", -1, 0, 0);

        sim.createClient(localNet1);
        graph.addNode("client1").addAttribute("ui.class", "clients");
        Node client1Node = graph.getNode("client1");
        client1Node.setAttribute("xyz", -3, 0, 0);

        graph.addEdge("iterConn1", "ispLink", "router1");
        graph.addEdge("intraConn1", "router1", "localNet1");
        graph.addEdge("intranet1", "localNet1", "client1");

        var localNet2 = sim.createNetwork();
        graph.addNode("localNet2").addAttribute("ui.class", "localNets");
        Node localNet2Node = graph.getNode("localNet2");
        localNet2Node.setAttribute("xyz", 2, 0, 0);


        sim.createServer(localNet2);
        graph.addNode("server1").addAttribute("ui.class", "servers");
        Node server1Node = graph.getNode("server1");
        server1Node.setAttribute("xyz", 3, 0, 0);

        sim.createServer(localNet2);
        graph.addNode("server2").addAttribute("ui.class", "servers");
        Node server2Node = graph.getNode("server2");
        server2Node.setAttribute("xyz", 3, 1, 0);

        sim.createServer(localNet2);
        graph.addNode("server3").addAttribute("ui.class", "servers");
        Node server3Node = graph.getNode("server3");
        server3Node.setAttribute("xyz", 3, -1, 0);

        sim.createRouter(localNet2, ispLink);
        graph.addNode("router2").addAttribute("ui.class", "routers");
        Node router2Node = graph.getNode("router2");
        router2Node.setAttribute("xyz", 1, 0, 0);


        graph.addEdge("iterConn2", "ispLink", "router2");
        graph.addEdge("intraConn2", "router2", "localNet2");
        graph.addEdge("intranet2.1", "localNet2", "server1");
        graph.addEdge("intranet2.2", "localNet2", "server2");
        graph.addEdge("intranet2.3", "localNet2", "server3");


        graph.display(false);

        sim.start("/tmp/ProtSimTest.pcap");

        System.out.println("Simulation running for 10s...");

        Thread.sleep(10000);

        sim.stop();
//        graph.clear();
        System.out.println("Simulation stopped.");
    }
}
