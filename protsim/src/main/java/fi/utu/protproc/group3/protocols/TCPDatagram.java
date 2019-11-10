package fi.utu.protproc.group3.protocols;

/**
 * Represents a TCP Datagram
 */
public interface TCPDatagram {
  static TCPDatagram create(int destinationPort, int sourcePort, boolean ACK, boolean RST, boolean SYN, boolean FIN, byte[] payload) {
    throw new UnsupportedOperationException();
  }

  static TCPDatagram parse(byte[] pdu) {
    throw new UnsupportedOperationException();
  }

  int getDestinationPort();
  int getSourcePort();
  int getSeqN();
  int getAckN();
  int getChecksum();

  short getHeaderLength(); // between 5 and 15 (header between 20-60 bytes).

  byte[] getPayload();

  /**
   * Serializes the packet to transmit over the wire.
   */
  byte[] serialize();
}
