package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;
import fi.utu.protproc.group3.graph.GraphAttributes;
import fi.utu.protproc.group3.nodes.*;
import fi.utu.protproc.group3.routing.TableRow;
import fi.utu.protproc.group3.utils.AddressGenerator;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimulationImpl implements SimulationBuilder, Simulation {
    private final Random random = new Random(1337);
    private final Logger rootLogger;
    private final Map<String, Network> networks = new HashMap<>();
    private final Map<String, NetworkNode> nodes = new HashMap<>();
    private String description;
    private String name;
    private FileOutputStream pcapStream;
    private List<ServerNode> servers;
    private MultiGraph graph;
    private Viewer viewer;

    public SimulationImpl() {
        this.rootLogger = Logger.getAnonymousLogger();
    }

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
            public <T extends NetworkNode> T node(String name) {
                return (T) nodes.get(name);
            }

            @Override
            public Simulation simulation() {
                return simulation;
            }
        };

        name = configuration.getName();
        description = configuration.getDescription();
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        graph = new MultiGraph(name);

        var autonomousSystems = new HashMap<Integer, AutonomousSystem>();

        if (configuration.getNetworks() != null) {
            for (var netConf : configuration.getNetworks()) {
                var net = new NetworkImpl(context, netConf);
                networks.put(netConf.getName(), net);

                var graphNode = graph.addNode(netConf.getName());
                graphNode.addAttribute(GraphAttributes.CLASS, "networks");
                graphNode.setAttribute(GraphAttributes.OBJECT, net);
                graphNode.setAttribute(GraphAttributes.LABEL, net.getNetworkName() + " (" + net.getNetworkAddress() + ")");

                if (netConf.getAutonomousSystem() != 0) {
                    autonomousSystems.computeIfAbsent(netConf.getAutonomousSystem(), AutonomousSystem::new).networks.add(graphNode);
                }

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

                if (routerConf.getAutonomousSystem() != 0) {
                    autonomousSystems.computeIfAbsent(routerConf.getAutonomousSystem(), AutonomousSystem::new).routers.add(gn);
                }

                node.getInterfaces().forEach(ethernetInterface -> {
                    var networkName = ethernetInterface.getNetwork().getNetworkName();
                    graph.addEdge(node.getHostname() + networkName, node.getHostname(), networkName).addAttributes(Map.of(
                            GraphAttributes.Edges.METRIC, ethernetInterface.getNetwork().getAutonomousSystem() == routerConf.getAutonomousSystem() ? 10.0 : 100.0
                    ));
                });
            }
        }

        generateStaticRoutes(autonomousSystems);

        return simulation;
    }

    private void generateStaticRoutes(HashMap<Integer, AutonomousSystem> autonomousSystems) {
        var apsp = new APSP(graph);
        apsp.setDirected(false);
        apsp.setWeightAttributeName(GraphAttributes.Edges.METRIC);
        apsp.compute();

        autonomousSystems.values().stream()
                .filter(as -> as.networks.size() >= 2 && as.routers.size() >= 1)
                .forEach(as -> {
                    as.routers.stream().forEach(r -> {
                        var router = (RouterNode) r.getAttribute(GraphAttributes.OBJECT);
                        var apspInfo = (APSP.APSPInfo) r.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
                        as.networks.stream().forEach(net -> {
                            var target = (Network) net.getAttribute(GraphAttributes.OBJECT);
                            var path = apspInfo.getShortestPathTo(net.getId());
                            var nodePath = path.getNodePath();
                            var network = (Network) nodePath.get(1).getAttribute(GraphAttributes.OBJECT);
                            var outIntf = router.getInterfaces().stream().filter(i -> i.getNetwork() == network).findAny();
                            var metric = (int) Math.ceil(path.getPathWeight(GraphAttributes.Edges.METRIC));
                            RouterNode nextHop = null;
                            Optional<EthernetInterface> nextHopIntf = Optional.empty();
                            if (nodePath.size() > 2) {
                                nextHop = nodePath.get(2).getAttribute(GraphAttributes.OBJECT);
                                nextHopIntf = nextHop.getInterfaces().stream().filter(i -> i.getNetwork() == network).findAny();
                                if (nextHop.getAutonomousSystem() != router.getAutonomousSystem() || network.getAutonomousSystem() != router.getAutonomousSystem()) {
                                    System.out.println("Route from " + router.getHostname() + " to network " + network.getNetworkName() + " goes across AS boundaries. No static routes generated.");
                                } else if (outIntf.isPresent() && nextHopIntf.isPresent()) {
                                    router.getRoutingTable().insertRow(TableRow.create(
                                            target.getNetworkAddress(),
                                            nextHopIntf.get().getIpAddress(),
                                            metric, (short) 0, (short) 0, outIntf.get()
                                    ));
                                }
                            } else {
                                router.getRoutingTable().insertRow(TableRow.create(
                                        target.getNetworkAddress(), null, metric, (short) 0, (short) 0, outIntf.get()
                                ));
                            }
                        });
                    });
                });
    }

    @Override
    public Logger getRootLogger() {
        return rootLogger;
    }

    @Override
    public <T extends NetworkNode> T getNode(String name) {
        return (T) nodes.get(name);
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
    public void start(String pcapFile) {
        if (pcapFile != null) {
            try {
                this.pcapStream = new FileOutputStream(pcapFile);

                var buf = ByteBuffer.allocate(24)
                        .putInt(0xa1b2c3d4)
                        .putShort((short) 2).putShort((short) 4)
                        .putInt(0)
                        .putInt(0)
                        .putInt(65535)
                        .putInt(1);

                pcapStream.write(buf.array());

                recording = Flux.merge((Iterable<Flux<byte[]>>) networks.values().stream().map(Network::getFlux)::iterator)
                        .subscribe(this::recordPacket);
            } catch (IOException e) {
                rootLogger.severe("Error while opening PCAP file: " + e);
                pcapStream = null;
            }
        }

        for (var node : nodes.values()) {
            node.start();
        }
    }

    @Override
    public void show() {
        if (viewer == null) {
            graph.setAttribute("ui.antialias");

            var stylePath = new File(System.getProperty("user.dir"), "styles");
            try {
                var styleSheet = new BufferedInputStream(new FileInputStream(new File(stylePath, "style.css")));
                var css = new String(styleSheet.readAllBytes(), "UTF-8").replace("url('./", "url('" + stylePath.getAbsolutePath().replace('\\', '/') + "/");
                graph.setAttribute("ui.stylesheet", css);

                viewer = graph.display();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        if (recording != null) {
            recording.dispose();
            recording = null;
        }

        if (pcapStream != null) {
            try {
                pcapStream.close();
            } catch (IOException e) {
                rootLogger.severe("Error while closing PCAP file: " + e);
            } finally {
                pcapStream = null;
            }
        }

        Schedulers.shutdownNow();
    }

    private Disposable recording;

    private void recordPacket(byte[] pdu) {
        if (pcapStream != null) {
            long timeStamp = System.currentTimeMillis();
            var buf = ByteBuffer.allocate(16 + pdu.length)
                    .putInt((int) timeStamp / 1000)
                    .putInt((int) ((timeStamp % 1000) * 1000))
                    .putInt(pdu.length)
                    .putInt(pdu.length)
                    .put(pdu);

            synchronized (pcapStream) {
                try {
                    pcapStream.write(buf.array());
                } catch (IOException e) {
                    rootLogger.severe("Error while recording packet: " + e);
                }
            }
        }
    }

    class AutonomousSystem {
        public final int id;
        public final Set<Node> routers = new HashSet<>();
        public final Set<Node> networks = new HashSet<>();

        public AutonomousSystem(int id) {
            this.id = id;
        }
    }
}
