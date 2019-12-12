package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.utils.IPAddress;
import reactor.core.publisher.Flux;

/**
 * Simulates an IEEE 802.3 ethernet interface as well as supporting protocols such as ARP.
 */
public interface EthernetInterface {
    /**
     * Gets the interface's ethernet address.
     */
    byte[] getAddress();

    /**
     * Gets the IP addresses
     */
    IPAddress getIpAddress();

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
     */
    Flux<byte[]> getFlux();

    /**
     * Gets the underlying network connection.
     */
    Network getNetwork();

    NetworkNode getHost();

}
