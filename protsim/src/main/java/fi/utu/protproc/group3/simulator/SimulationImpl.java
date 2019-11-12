package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.utils.AddressGenerator;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimulationImpl implements Simulation {
    private final Random random = new Random(1337);
    private final Logger rootLogger;
    private final AddressGenerator generator = new AddressGenerator(random);
    private final List<Network> networks = new ArrayList<>();
    private final List<NetworkNode> nodes = new ArrayList<>();
    private FileOutputStream pcapStream;
    private List<ServerNode> servers;

    public SimulationImpl() {
        this.rootLogger = Logger.getAnonymousLogger();
    }

    @Override
    public Network createNetwork() {
        var result = Network.create(this, generator.networkAddress());
        networks.add(result);

        return result;
    }

    @Override
    public RouterNode createRouter(Network... networks) {
        var result = RouterNode.create(this, generator, networks);
        nodes.add(result);

        return result;
    }

    @Override
    public ClientNode createClient(Network network) {
        try {
            var result = ClientNode.create(this, generator, network);
            nodes.add(result);

            return result;
        } catch (UnknownHostException e) {
            rootLogger.severe("Error while creating client: " + e);

            return null;
        }
    }

    @Override
    public ServerNode createServer(Network network) {
        var result = ServerNode.create(this, generator, network);
        nodes.add(result);

        return result;
    }

    @Override
    public Logger getRootLogger() {
        return rootLogger;
    }

    @Override
    public Collection<NetworkNode> getNodes() {
        return Collections.unmodifiableCollection(nodes);
    }

    @Override
    public Collection<Network> getNetworks() {
        return Collections.unmodifiableCollection(networks);
    }

    @Override
    public ServerNode getRandomServer() {
        if (servers == null) {
            servers = nodes.stream()
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

                recording = Flux.merge((Iterable<Flux<byte[]>>) networks.stream().map(n -> n.getFlux())::iterator)
                        .subscribe(this::recordPacket);
            } catch (IOException e) {
                rootLogger.severe("Error while opening PCAP file: " + e);
                pcapStream = null;
            }
        }

        for (var node : nodes) {
            node.start();
        }
    }

    @Override
    public void stop() {
        for (var node : nodes) {
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
