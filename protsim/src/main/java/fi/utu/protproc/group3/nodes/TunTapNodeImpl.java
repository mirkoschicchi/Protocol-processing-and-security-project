package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.configuration.TapConfiguration;
import fi.utu.protproc.group3.protocols.EthernetFrame;
import fi.utu.protproc.group3.protocols.IPv6Packet;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.SimulationBuilderContext;
import io.github.isotes.net.tun.io.Packet;
import io.github.isotes.net.tun.io.TunDevice;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TunTapNodeImpl extends NetworkNodeImpl {
    private final TapConfiguration configuration;
    private volatile boolean running;
    private TunDevice device;
    private Thread listenThread;

    public TunTapNodeImpl(SimulationBuilderContext context, TapConfiguration configuration, Network network) {
        super(context, configuration, network);

        this.configuration = configuration;
    }

    @Override
    public void start() {
        running = true;

        try {
            this.device = TunDevice.open(configuration.getDevice());

            listenThread = new Thread(this::listen);
            listenThread.start();

            getInterface().getFlux().subscribe(this::sendPacket);
        } catch (IOException e) {
            var helpMsg = new StringBuilder()
                    .append("# Failed to open tun device ").append(configuration.getDevice()).append(": ").append(e.getMessage()).append('\n')
                    .append("# To create the device (Linux only):\n")
                    .append("sudo ip tuntap add dev ").append(configuration.getDevice()).append(" mode tun user $USER group $USER\n")
                    .append("sudo ip -6 addr add ").append(getIpAddress()).append('/').append(getInterface().getNetwork().getNetworkAddress().getPrefixLength()).append(" dev ").append(configuration.getDevice()).append("\n")
                    .append("sudo ip link set dev ").append(configuration.getDevice()).append(" up\n");

            for (var route : getRoutingTable().getRows()) {
                helpMsg.append("sudo ip r add " + route.getPrefix() + " dev ").append(configuration.getDevice()).append(" via ").append(route.getNextHop()).append('\n');
            }

            helpMsg
                    .append("# Restart simulation to connect.\n\n")
                    .append("# And to remove the device:\n")
                    .append("sudo ip tuntap del dev ").append(configuration.getDevice()).append(" mode tun\n");

            System.err.print(helpMsg.toString());
        }
    }

    @Override
    public void shutdown() {
        running = false;
        if (listenThread != null) {
            try {
                listenThread.interrupt();
                listenThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (device != null) {
            try {
                device.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            device = null;
        }
    }

    private void sendPacket(byte[] bytes) {
        try {
            var frame = EthernetFrame.parse(bytes);
            var packet = new Packet(ByteBuffer.wrap(frame.getPayload()));
            assert packet.isIpv6();
            device.write(packet);
        } catch (IOException e) {
            e.printStackTrace();
            shutdown();
        }
    }

    private void listen() {
        while (running) {
            try {
                var packet = device.read();
                if (packet.isIpv6()) {
                    sendPacket(IPv6Packet.parse(packet.bytes()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                running = false;
            }
        }
    }
}
