package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.NetworkAddress;

public abstract class NetworkNodeImpl implements NetworkNode {
    private Iterable<EthernetInterface> interfaces;
    private Thread backgroundThread;
    private NetworkAddress networkAddress;

    public NetworkNodeImpl(Iterable<EthernetInterface> interfaces, Thread backgroundThread, NetworkAddress networkAddress) {
        this.interfaces = interfaces;
        this.backgroundThread = backgroundThread;
        this.networkAddress = networkAddress;
    }

    @Override
    public Iterable<EthernetInterface> getInterfaces() {
        return interfaces;
    }

    @Override
    public Thread getBackgroundThread() {
        return backgroundThread;
    }

    @Override
    public NetworkAddress getNetworkAddress() {
        return networkAddress;
    }

}
