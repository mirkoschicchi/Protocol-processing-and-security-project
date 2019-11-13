package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * Simulates an IEEE 802.3 ethernet interface as well as supporting protocols such as ARP.
 */
public interface EthernetInterface {
    static EthernetInterface create(byte[] address, Network network) {
        var result = new EthernetInterfaceImpl(address, network);
        network.addDevice(result);

        return result;
    }

    /**
     * Gets the interface's ethernet address.
     */
    byte[] getAddress();

    /**
     * Adds an IP address to the interface
     */
    void addIpAddress(IPAddress addr);

    /**
     * Gets all configured IP addresses
     */
    Collection<IPAddress> getIpAddresses();

    /**
     * Removes an IP address from the interface
     */
    void removeIpAddress(IPAddress addr);

    /**
     * Resolves an IP address using simulated ARP.
     * @param address The address to resolve.
     * @return The MAC address of the resolved address or null if not found.
     */
    byte[] resolveIpAddress(IPAddress address);

    /**
     * Transmits a PDU to the given MAC address
     * @param frame The frame to transmit
     */
    void transmit(byte[] frame);

    /**
     * Gets the queue with received frames.
     * @return
     */
    Flux<byte[]> getFlux();

    /**
     * Gets the underlying network connection.
     */
    Network getNetwork();
}
