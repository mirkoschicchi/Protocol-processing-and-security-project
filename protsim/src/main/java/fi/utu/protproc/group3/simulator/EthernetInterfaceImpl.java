package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.publisher.Flux;

import java.util.*;

public class EthernetInterfaceImpl implements EthernetInterface {
    private final byte[] address;
    private final Network network;
    private final List<IPAddress> ipAddresses = new ArrayList<>();

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
    public void addIpAddress(IPAddress addr) {
        ipAddresses.add(addr);
    }

    @Override
    public Collection<IPAddress> getIpAddresses() {
        return Collections.unmodifiableCollection(ipAddresses);
    }

    @Override
    public void removeIpAddress(IPAddress addr) {
        ipAddresses.remove(addr);
    }

    @Override
    public byte[] resolveIpAddress(IPAddress address) {
        Objects.requireNonNull(address);

        for (var dev : getNetwork().getDevices()) {
            if (dev.getIpAddresses().contains(address)) {
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
