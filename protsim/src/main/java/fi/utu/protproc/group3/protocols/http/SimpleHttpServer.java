package fi.utu.protproc.group3.protocols.http;

import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.protocols.tcp.Connection;

import java.nio.charset.StandardCharsets;

public class SimpleHttpServer extends Connection {
    public static final short DEFAULT_PORT = (short) 80;

    public SimpleHttpServer(NetworkNode node) {
        super(node);
    }

    @Override
    public void messageReceived(byte[] message) {
        super.messageReceived(message);

        var body = "<h1>Hello from " + node.getHostname() + "</h1>" +
                "<p>Your request:</p>" +
                "<pre>" + new String(message, StandardCharsets.UTF_8) + "</pre>";

        var reply = "HTTP/1.0 418 I'm a simple http server\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" + body;

        send(reply.getBytes());
    }
}
