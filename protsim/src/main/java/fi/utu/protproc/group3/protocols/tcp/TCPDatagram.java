package fi.utu.protproc.group3.protocols.tcp;

import java.util.Objects;

/**
 * Represents a TCP Datagram
 */
public interface TCPDatagram {
    /*
    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |          Source Port          |       Destination Port        |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                        Sequence Number                        |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                    Acknowledgment Number                      |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |  Data |           |U|A|P|R|S|F|                               |
    | Offset| Reserved  |R|C|S|S|Y|I|            Window             |
    |       |           |G|K|H|T|N|N|                               |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |           Checksum            |         Urgent Pointer        |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                    Options                    |    Padding    |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                             data                              |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    static TCPDatagram create(short sourcePort, short destinationPort, int seqN, int ackN,
                              byte dataOffset, short flags, short window, short checksum, short urgentPointer,
                              byte[] optionsAndPadding, byte[] payload) {
        return new TCPDatagramImpl(sourcePort, destinationPort, seqN, ackN,
                dataOffset, flags, window, checksum, urgentPointer, optionsAndPadding, payload);
    }

    static TCPDatagram parse(byte[] pdu){
        Objects.requireNonNull(pdu);
        return TCPDatagramImpl.parse(pdu);
    }

    // FLAGS
    // 63 = 111111 (all flags set)
    short FIN = (short)1;
    short SYN = (short) (1 << 1);
    short RST = (short) (1 << 2);
    short PSH = (short) (1 << 3);
    short ACK = (short) (1 << 4);
    short URG = (short) (1 << 5);

    short getDestinationPort();
    short getSourcePort();
    int getSeqN();
    int getAckN();
    byte getDataOffset();
    short getFlags();
    short getWindow();
    int getChecksum();
    int getUrgentPointer();
    byte[] getOptionsAndPadding();
    byte[] getPayload();

    byte[] serialize();
}
