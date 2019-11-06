package fi.utu.protproc.group3.simulator;

import java.net.InetAddress;
import java.util.Queue;

/**
 * Simulates an IEEE 802.3 ethernet interface as well as supporting protocols such as ARP.
 */
public interface EthernetInterface {
    static EthernetInterface create(byte[] address, Network connection) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the interface's ethernet address.
     */
    byte[] getAddress();

    /**
     * Adds an IP address to the interface
     */
    void addInetAddress(InetAddress addr);

    /**
     * Gets all configured IP addresses
     */
    Iterable<InetAddress> getInetAddresses();

    /**
     * Removes an IP address from the interface
     */
    void removeInetAddress(InetAddress addr);

    /**
     * Resolves an IP address using simulated ARP.
     * @param address The address to resolve.
     * @return The MAC address of the resolved address or null if not found.
     */
    byte[] resolveInetAddress(InetAddress address);

    /**
     * Transmits a PDU to the given MAC address
     * @param frame The frame to transmit
     */
    void transmit(byte[] frame);

    /**
     * Gets the queue with received frames.
     */
    Queue<byte[]> getReceiverQueue();
}
