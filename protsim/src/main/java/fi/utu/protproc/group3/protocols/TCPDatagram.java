package fi.utu.protproc.group3.protocols;

/**
 * Represents a TCP Datagram and should be set by a router to send data to another router
 */
public interface TCPDatagram {
    static TCPDatagram create(int destinationPort, int sourcePort, byte flags, long seqN, long ackN, byte[] payload) {
      throw new UnsupportedOperationException();
    }

    static TCPDatagram parse(byte[] pdu) {
        throw new UnsupportedOperationException();
    }

    byte SYN = 0x1;
    byte ACK = 0x2;
    byte FIN = 0x3;
//    byte RST = 0x4;
    byte SYNACK = 0x5;
    byte FINACK = 0x6;

    int getDestinationPort();
    int getSourcePort();
    long getSeqN();
    long getAckN();
    byte getFlags();
    int getChecksum();
    byte[] getPayload();

    /**
     * Serializes the packet to transmit over the wire.
     */
    byte[] serialize();
}
