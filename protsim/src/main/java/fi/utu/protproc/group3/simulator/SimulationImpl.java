package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;
import fi.utu.protproc.group3.nodes.*;
import fi.utu.protproc.group3.utils.AddressGenerator;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

public class SimulationImpl implements SimulationBuilder, Simulation {
    private final Random random = new Random(1337);
    private final Logger rootLogger;
    private final Map<String, Network> networks = new HashMap<>();
    private final Map<String, NetworkNode> nodes = new HashMap<>();
    private String description;
    private String name;
    private FileOutputStream pcapStream;
    private List<ServerNode> servers;

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

        Graph graph = new MultiGraph(name);
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
                        "node.networks {"+
                        "size: 60px;"+
                        "fill-image: url('" + path + "kisspng-cloud.png" + "');" +
                        "}"+
                        "node.routers {"+
                        "size: 40px, 30px;"+
                        "fill-image: url('" + path + "kisspng-wireless-router.png" + "');" +
                        "}"+
                        "node.clients {"+
                        "size: 30px;"+
                        "fill-image: url('" + path + "kisspng-computer.png" + "');" +
                        "}"+
                        "node.servers {"+
                        "size: 20px, 30px;"+
                        "fill-image: url('" + path + "kisspng-server.png" + "');" +
                        "}"+
                        "edge {"+
                        "shape: cubic-curve;"+
                        "arrow-shape: none;"+
                        "size: 3px;"+
                        "}";
        graph.addAttribute("ui.stylesheet", styleSheet);

        if (configuration.getNetworks() != null) {
            for (var netConf : configuration.getNetworks()) {
                var net = new NetworkImpl(context, netConf);
                networks.put(netConf.getName(), net);
                graph.addNode(netConf.getName()).addAttribute("ui.class", "networks");

                netConf.getActualServers()
                        .map(conf -> new ServerNodeImpl(context, conf, net))
                        .forEach(s -> {
                            nodes.put(s.getHostname(), s);
                            graph.addNode(s.getHostname()).addAttribute("ui.class", "servers");
                            graph.addEdge(s.getHostname() + netConf.getName(), s.getHostname(), netConf.getName());
                        });

                netConf.getActualClients()
                        .map(conf -> new ClientNodeImpl(context, conf, net))
                        .forEach(s -> {
                            nodes.put(s.getHostname(), s);
                            graph.addNode(s.getHostname()).addAttribute("ui.class", "clients");
                            graph.addEdge(s.getHostname() + netConf.getName(), s.getHostname(), netConf.getName());
                        });
            }
        }

        if (configuration.getRouters() != null) {
            for (var routerConf : configuration.getRouters()) {
                var node = new RouterNodeImpl(context, routerConf);
                nodes.put(node.getHostname(), node);
                graph.addNode(node.getHostname()).addAttribute("ui.class", "routers");

                node.getInterfaces().forEach(ethernetInterface -> {
                    var networkName = ethernetInterface.getNetwork().getNetworkName();
                    graph.addEdge(node.getHostname() + networkName, node.getHostname(), networkName);
                });
            }
        }

        graph.display();
        return simulation;
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
}
