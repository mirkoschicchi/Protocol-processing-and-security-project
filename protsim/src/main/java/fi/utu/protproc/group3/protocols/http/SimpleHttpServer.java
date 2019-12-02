package fi.utu.protproc.group3.protocols.http;

import fi.utu.protproc.group3.protocols.tcp.Connection;
import fi.utu.protproc.group3.simulator.EthernetInterface;

import java.io.UnsupportedEncodingException;

public class SimpleHttpServer extends Connection {
    public static final short DEFAULT_PORT = (short) 80;

    public SimpleHttpServer(EthernetInterface ethernetInterface) {
        super(ethernetInterface);
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        try {
            var body = "<h1>Hello from " + ethernetInterface.getHost().getHostname() + "</h1>" +
                    "<p>Your request:</p>" +
                    "<pre>" + new String(message, "UTF-8") + "</pre>";

            var reply = "HTTP/1.0 418 I'm a simple http server\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" + body;

            send(reply.getBytes());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
