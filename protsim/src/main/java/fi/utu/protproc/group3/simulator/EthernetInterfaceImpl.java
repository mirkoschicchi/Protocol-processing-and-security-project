package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.publisher.Flux;

import java.util.*;

public class EthernetInterfaceImpl implements EthernetInterface {
    private final byte[] address;
    private final Network network;
    private final IPAddress ipAddress;

    public EthernetInterfaceImpl(byte[] address, Network network, IPAddress ipAddress) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(network);
        Objects.requireNonNull(ipAddress);

        this.address = address;
        this.network = network;
        network.addDevice(this);
        this.ipAddress = ipAddress;
    }

    @Override
    public byte[] getAddress() {
        return address;
    }

    @Override
    public IPAddress getIpAddress() {
        return ipAddress;
    }

    @Override
    public byte[] resolveIpAddress(IPAddress address) {
        Objects.requireNonNull(address);

        for (var dev : getNetwork().getDevices()) {
            if (dev.getIpAddress().equals(address)) {
                return dev.getAddress();
            }
        }

        return null;
    }

    @Override
    public void transmit(byte[] frame) {
        Objects.requireNonNull(frame);

        getNetwork().transmit(frame);
    }

    @Override
    public Flux<byte[]> getFlux() {
        return getNetwork().getFlux()
                .filter(p -> p.length > 6 && p[0] == address[0] && p[1] == address[1] && p[2] == address[2]
                        && p[3] == address[3] && p[4] == address[4] && p[5] == address[5]);
    }

    @Override
    public Network getNetwork() {
        return network;
    }
}
