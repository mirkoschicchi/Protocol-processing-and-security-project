package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;
import fi.utu.protproc.group3.graph.GraphAttributes;
import fi.utu.protproc.group3.nodes.*;
import fi.utu.protproc.group3.userinterface.UserGUI;
import fi.utu.protproc.group3.utils.AddressGenerator;
import fi.utu.protproc.group3.utils.SimulationReference;
import javafx.application.Application;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.view.Viewer;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimulationImpl implements SimulationBuilder, Simulation {
    private static final Logger LOGGER = Logger.getLogger(SimulationImpl.class.getName());

    private final Random random = new Random(1337);
    private final Map<String, Network> networks = new HashMap<>();
    private final Map<String, NetworkNode> nodes = new HashMap<>();
    private final Object pcapLock = new Object();
    private FileOutputStream pcapStream;
    private Map<Network, Integer> pcapInterfaces;
    private List<ServerNode> servers;
    private MultiGraph graph;
    private Viewer viewer;

    @Override
    public Simulation load(SimulationConfiguration configuration) {
        Objects.requireNonNull(configuration);

        var generator = new AddressGenerator(random);
        var simulation = this;

        var context = new SimulationBuilderContext() {
            @Override
            public AddressGenerator generator() {
                return generator;
            }

            @Override
            public Network network(String name) {
                return networks.get(name);
            }

            @Override
            public Simulation simulation() {
                return simulation;
            }
        };

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        graph = new MultiGraph(configuration.getName());

        var autonomousSystems = new HashMap<Integer, AutonomousSystem>();

        if (configuration.getNetworks() != null) {
            for (var netConf : configuration.getNetworks()) {
                var net = new NetworkImpl(context, netConf);
                networks.put(netConf.getName(), net);

                var graphNode = graph.addNode(netConf.getName());
                graphNode.addAttribute(GraphAttributes.CLASS, "networks");
                graphNode.setAttribute(GraphAttributes.OBJECT, net);
                graphNode.setAttribute(GraphAttributes.LABEL, net.getNetworkName() + " (" + net.getNetworkAddress() + ")");

                autonomousSystems.computeIfAbsent(netConf.getAutonomousSystem(), AutonomousSystem::new).networks.add(graphNode);

                Function<NetworkNode, Node> registerNode = n -> {
                    nodes.put(n.getHostname(), n);

                    var result = graph.addNode(n.getHostname());
                    result.setAttribute(GraphAttributes.OBJECT, n);
                    result.setAttribute(GraphAttributes.LABEL, n.getHostname());

                    graph.addEdge(n.getHostname() + netConf.getName(), n.getHostname(), netConf.getName());

                    return result;
                };

                netConf.getActualServers()
                        .map(conf -> new ServerNodeImpl(context, conf, net))
                        .forEach(s -> registerNode.apply(s).addAttribute(GraphAttributes.CLASS, "servers"));

                netConf.getActualClients()
                        .map(conf -> new ClientNodeImpl(context, conf, net))
                        .forEach(s -> registerNode.apply(s).addAttribute(GraphAttributes.CLASS, "clients"));

                if (netConf.getTap() != null) {
                    var tapNode = new TunTapNodeImpl(context, netConf.getTap(), net);
                    nodes.put(tapNode.getHostname(), tapNode);
                }
            }
        }

        if (configuration.getRouters() != null) {
            for (var routerConf : configuration.getRouters()) {
                var node = new RouterNodeImpl(context, routerConf);
                nodes.put(node.getHostname(), node);

                var gn = graph.addNode(node.getHostname());
                gn.addAttributes(Map.of(
                        GraphAttributes.CLASS, "routers",
                        GraphAttributes.OBJECT, node,
                        GraphAttributes.LABEL, node.getHostname()
                ));

                autonomousSystems.computeIfAbsent(routerConf.getAutonomousSystem(), AutonomousSystem::new).routers.add(gn);

                node.getInterfaces().forEach(ethernetInterface -> {
                    var networkName = ethernetInterface.getNetwork().getNetworkName();
                    graph.addEdge(node.getHostname() + networkName, node.getHostname(), networkName).addAttributes(Map.of(
                            GraphAttributes.Edges.METRIC, ethernetInterface.getNetwork().getAutonomousSystem() == routerConf.getAutonomousSystem() ? 10.0 : 100.0
                    ));
                });
            }
        }

        generateStaticRoutes(autonomousSystems);

        nodes.values().stream()
                .filter(n -> n instanceof RouterNode)
                .map(n -> (RouterNode) n)
                .forEach(router -> {
                    var configurator = router.getConfigurator();
                    for (var intf : router.getInterfaces()) {
                        for (var peerDev : intf.getNetwork().getDevices()) {
                            if (peerDev != intf && peerDev.getHost() instanceof RouterNode) {
                                var secondDegreeNeighbors = peerDev.getHost().getInterfaces().stream()
                                        .flatMap(i -> i.getNetwork().getDevices().stream())
                                        .filter(i -> i.getHost() instanceof RouterNode && i.getHost() != router && i != peerDev)
                                        .map(EthernetInterface::getIpAddress)
                                        .collect(Collectors.toUnmodifiableList());

                                configurator.createPeering(intf, peerDev.getIpAddress(), secondDegreeNeighbors);
                            }
                        }
                    }

                    configurator.finalizeConfiguration();
                });

        return simulation;
    }

    private void generateStaticRoutes(Map<Integer, AutonomousSystem> autonomousSystems) {
        Objects.requireNonNull(autonomousSystems);

        var apsp = new APSP(graph);

        apsp.setDirected(false);
        apsp.setWeightAttributeName(GraphAttributes.Edges.METRIC);
        apsp.compute();

        autonomousSystems.values().stream()
                .filter(as -> as.id != 0 && as.networks.size() >= 1 && as.routers.size() >= 1)
                .forEach(as -> as.routers.forEach(r -> {
                    var router = (RouterNode) r.getAttribute(GraphAttributes.OBJECT);
                    var configurator = router.getConfigurator();
                    var apspInfo = (APSP.APSPInfo) r.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
                    as.networks.forEach(net -> {
                        var target = (Network) net.getAttribute(GraphAttributes.OBJECT);
                        var path = apspInfo.getShortestPathTo(net.getId());
                        var nodePath = path.getNodePath();
                        var network = (Network) nodePath.get(1).getAttribute(GraphAttributes.OBJECT);
                        var outIntf = router.getInterfaces().stream().filter(i -> i.getNetwork() == network).findAny();
                        var metric = (int) Math.ceil(path.getPathWeight(GraphAttributes.Edges.METRIC));
                        if (nodePath.size() > 2) {
                            RouterNode nextHop = nodePath.get(2).getAttribute(GraphAttributes.OBJECT);
                            var nextHopIntf = nextHop.getInterfaces().stream().filter(i -> i.getNetwork() == network).findAny();
                            if (nextHop.getAutonomousSystem() != router.getAutonomousSystem() || network.getAutonomousSystem() != router.getAutonomousSystem()) {
                                System.err.println("Route from " + router.getHostname() + " to network " + network.getNetworkName() + " goes across AS boundaries. No static routes generated.");
                            } else if (outIntf.isPresent() && nextHopIntf.isPresent()) {
                                configurator.createStaticRoute(target.getNetworkAddress(), outIntf.get(), nextHopIntf.get().getIpAddress(), metric);
                            }
                        }
                    });
                }));
    }

    @Override
    public NetworkNode getNode(String name) {
        return nodes.get(name);
    }

    @Override
    public Collection<NetworkNode> getNodes() {
        return nodes.values();
    }

    @Override
    public Network getNetwork(String name) {
        return networks.get(name);
    }

    @Override
    public Collection<Network> getNetworks() {
        return networks.values();
    }

    @Override
    public ServerNode getRandomServer() {
        if (servers == null) {
            servers = nodes.values().stream()
                    .filter(n -> n instanceof ServerNode)
                    .map(n -> (ServerNode) n)
                    .collect(Collectors.toList());
        }

        return servers.get(random.nextInt(servers.size()));
    }

    @Override
    public void start() {
        start(null, null);
    }

    @Override
    public void start(String pcapFile, String network) {
        if (pcapFile != null) {
            try {
                this.pcapStream = new FileOutputStream(pcapFile);

                var shbContent = ByteBuffer.allocate(16)
                        .putInt(0x1A2B3C4D)
                        .putShort((short) 1).putShort((short) 0)
                        .putLong(-1);

                writeBlock(0x0A0D0D0A, shbContent);

                pcapInterfaces = new HashMap<>();

                var capture = new ArrayList<Network>();
                if (network != null) {
                    capture.add(getNetwork(network));
                } else {
                    capture.addAll(networks.values());
                }

                capture.forEach(n -> {
                    int index = pcapInterfaces.size();
                    pcapInterfaces.put(n, index);

                    var name = (n.getNetworkName()).getBytes();
                    var block = ByteBuffer.allocate(44 + name.length)
                            .putShort((short) 1).putShort((short) 0) // link type (ethernet)
                            .putInt(0) // snap len (unlimited)
                            .putShort((short) 5).putShort((short) 17) // if_IPv6addr
                            .put(n.getNetworkAddress().getAddress().toArray())
                            .put((byte) n.getNetworkAddress().getPrefixLength()).put((byte) 0).put((byte) 0).put((byte) 0)
                            .putShort((short) 9).putShort((short) 1) // if_tsresol
                            .put((byte) 0x03).put((byte) 0).put((byte) 0).put((byte) 0)

                            // Name last so we don't have to worry about padding here
                            .putShort((short) 2).putShort((short) name.length) // if_name
                            .put(name);

                    writeBlock(0x00000001, block);
                });

                recording = Flux.merge((Iterable<Flux<byte[]>>)
                        (capture.stream().map(n -> n.getFlux().doOnEach(pdu -> this.recordPacket(n, pdu.get()))))::iterator
                ).subscribe();
            } catch (IOException e) {
                LOGGER.severe("Error while opening PCAP file: " + e);
                pcapStream = null;
            }
        }

        for (var net : networks.values()) {
            net.start();
        }

        for (var node : nodes.values()) {
            node.start();
        }
    }

    @Override
    public void show() {
        if (viewer == null) {
            graph.setAttribute("ui.quality");
            graph.setAttribute("ui.antialias");

            var stylePath = new File(System.getProperty("user.dir"), "styles");
            try {
                var styleSheet = new BufferedInputStream(new FileInputStream(new File(stylePath, "style.css")));
                var css = new String(styleSheet.readAllBytes(), StandardCharsets.UTF_8).replace("url('./", "url('" + stylePath.getAbsolutePath().replace('\\', '/') + "/");
                graph.setAttribute("ui.stylesheet", css);

                //viewer = graph.display();
                viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
                GraphRenderer renderer = Viewer.newGraphRenderer();
                viewer.addView(Viewer.DEFAULT_VIEW_ID, renderer, false);
                Layout layout = Layouts.newLayoutAlgorithm();
                viewer.enableAutoLayout(layout);


                //new UserInterfaceManager(viewer, graph, nodes);
                SimulationReference.simulation = this;
                SimulationReference.nodes = nodes;
                Application.launch(UserGUI.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeBlock(int type, ByteBuffer data) {
        Objects.requireNonNull(data);

        try {
            var buf = ByteBuffer.allocate(12 + 4 * ((data.limit() + 3) / 4));
            buf.putInt(type)
                    .putInt(buf.limit())
                    .put(data.array())
                    .position(buf.limit() - 4)
                    .putInt(buf.limit());

            pcapStream.write(buf.array());
        } catch (IOException e) {
            e.printStackTrace();
            pcapStream = null;
            recording.dispose();
        }
    }

    @Override
    public void close() {
        if (viewer != null) {
            viewer.close();
        }
    }

    @Override
    public void stop() {
        for (var node : nodes.values()) {
            node.shutdown();
        }

        for (var net : networks.values()) {
            net.shutdown();
        }

        if (recording != null) {
            recording.dispose();
            recording = null;
        }

        if (pcapStream != null) {
            try {
                pcapStream.close();
            } catch (IOException e) {
                LOGGER.severe("Error while closing PCAP file: " + e);
            } finally {
                pcapStream = null;
            }
        }

        Schedulers.shutdownNow();
    }

    private Disposable recording;

    private void recordPacket(Network network, byte[] pdu) {
        if (pcapStream != null) {
            long timeStamp = System.currentTimeMillis();
            var index = pcapInterfaces.get(network);

            var buf = ByteBuffer.allocate(20 + pdu.length)
                    .putInt(index)
                    .putInt((int) (timeStamp >> 32))
                    .putInt((int) (timeStamp))
                    .putInt(pdu.length)
                    .putInt(pdu.length)
                    .put(pdu);

            synchronized (pcapLock) {
                writeBlock(6, buf);
            }
        }
    }

    public MultiGraph getGraph() {
        return graph;
    }

    public Viewer getViewer() {
        return viewer;
    }

    static class AutonomousSystem {
        final int id;
        final Set<Node> routers = new HashSet<>();
        final Set<Node> networks = new HashSet<>();

        AutonomousSystem(int id) {
            this.id = id;
        }
    }
}
