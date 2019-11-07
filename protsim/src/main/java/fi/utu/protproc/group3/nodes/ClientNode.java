package fi.utu.protproc.group3.nodes;

import java.net.InetAddress;

public interface ClientNode extends NetworkNode {
    /**
     * Simulates traffic with a server node (chosen by the simulation).
     */
    void simulateTrafficWith(InetAddress server, int tcpPort);
}
