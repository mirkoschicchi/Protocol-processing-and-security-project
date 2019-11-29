package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.utils.IPAddress;

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
                              short flags, short window, byte[] payload) {
        return new TCPDatagramImpl(sourcePort, destinationPort, seqN, ackN, flags, window, payload);
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

    short getFlags();
    short getWindow();

    short getChecksum();

    byte[] getPayload();

    // the TCP protocol hexadecimal representation is 0x6 and is found in the Next Header field of the IPv6 header
    // https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
    // Then it is needed the protocol length which is given by TCP header + payload
    // https://stackoverflow.com/questions/30858973/udp-checksum-calculation-for-ipv6-packet
    byte[] serialize(IPAddress sourceIP, IPAddress destinationIP);
}
