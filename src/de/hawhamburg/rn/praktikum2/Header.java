package de.hawhamburg.rn.praktikum2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Header for a message.
 */
public class Header {

  private byte[] srcIP, destIP, checksum;
  private byte[] header;

  // header for outgoing message
  public Header(Inet4Address srcIP, Inet4Address destIP, int checksum) throws IOException {
    this.srcIP = srcIP.getAddress();
    this.destIP = destIP.getAddress();
    ByteBuffer b = ByteBuffer.allocate(2);
    b.putInt(checksum);
    this.checksum = b.array();
    createOutgoingHeader();
  }

  // header for received message
  public Header(byte[] header) {
    if (header.length != 12) {
      throw new IllegalArgumentException("Wrong header size.\nExpected size: 12\nActual size: " + header.length);
    }
    this.header = header;
    createIncomingHeader();
  }

  /**
   * Creates a header for an outgoing message.
   */
  private void createOutgoingHeader() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(srcIP);
    outputStream.write(destIP);
    outputStream.write(checksum);
    header = outputStream.toByteArray();
  }

  /**
   * Creates a header for a received message.
   */
  private void createIncomingHeader() {
    srcIP = Arrays.copyOfRange(header, 0, 4);
    destIP = Arrays.copyOfRange(header, 4, 8);
    checksum = Arrays.copyOfRange(header, 8, 12);
  }

  public byte[] getHeader() {
    return header;
  }

  public Inet4Address getSourceIP() throws UnknownHostException {
    return (Inet4Address) Inet4Address.getByAddress(srcIP);
  }

  public Inet4Address getDestinationIP() throws UnknownHostException {
    return (Inet4Address) Inet4Address.getByAddress(destIP);
  }
}