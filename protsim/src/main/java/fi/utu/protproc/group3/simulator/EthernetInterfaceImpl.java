package fi.utu.protproc.group3.simulator;

import reactor.core.publisher.Flux;

import java.net.InetAddress;
import java.util.*;

public class EthernetInterfaceImpl implements EthernetInterface {
    private final byte[] address;
    private final Network network;
    private final List<InetAddress> inetAddresses = new ArrayList<>();

    EthernetInterfaceImpl(byte[] address, Network network) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(network);

        this.address = address;
        this.network = network;
    }

    @Override
    public byte[] getAddress() {
        return address;
    }

    @Override
    public void addInetAddress(InetAddress addr) {
        inetAddresses.add(addr);
    }

    @Override
    public Collection<InetAddress> getInetAddresses() {
        return Collections.unmodifiableCollection(inetAddresses);
    }

    @Override
    public void removeInetAddress(InetAddress addr) {
        inetAddresses.remove(addr);
    }

    @Override
    public byte[] resolveInetAddress(InetAddress address) {
        Objects.requireNonNull(address);

        for (var dev : getNetwork().getDevices()) {
            if (dev.getInetAddresses().contains(address)) {
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
