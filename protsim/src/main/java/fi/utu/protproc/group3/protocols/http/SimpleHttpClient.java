package fi.utu.protproc.group3.protocols.http;

import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.simulator.EthernetInterface;

public class SimpleHttpClient extends Connection {
    public SimpleHttpClient(EthernetInterface ethernetInterface) {
        super(ethernetInterface);
    }

    @Override
    public void connected(DatagramHandler.ConnectionState connectionState) {
        super.connected(connectionState);

        send("GET / HTTP/1.0".getBytes());

        System.out.println(connectionState.getDescriptor() + " SENT");
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        System.out.println(connectionState.getDescriptor() + " RECEIVED");

        close();
    }
}
