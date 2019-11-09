package fi.utu.protproc.group3.protocols;

/**
 * Represents an IPv6 packet.
 */
public interface TCPDatagram {
  static TCPDatagram create(byte[] destination, byte[] source, byte[] payload) {
    throw new UnsupportedOperationException();
  }

  static TCPDatagram parse(byte[] tpdu) {
    throw new UnsupportedOperationException();
  }

  byte[] getDestination();

  byte[] getSource();

  byte[] getPayload();

  /**
   * Serializes the packet to transmit over the wire.
   */
  byte[] serialize();
}
