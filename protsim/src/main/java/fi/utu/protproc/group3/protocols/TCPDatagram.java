package fi.utu.protproc.group3.protocols;

/**
 * Represents a TCP Datagram
 */
public interface TCPDatagram {
    static TCPDatagram create(int destinationPort, int sourcePort, byte flags, long seqN, long ackN, byte[] payload) {
        return new TCPDatagramImpl(destinationPort, sourcePort, flags, seqN, ackN, payload);
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

    byte[] serialize();
    TCPDatagram parse(byte[] pdu);
}
