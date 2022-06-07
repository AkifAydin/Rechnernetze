package de.hawhamburg.rn.praktikum2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Header {

  private byte[] srcPort, destPort, srcIP, destIP, checksum;
  private byte[] header;

  public Header(byte[] srcPort, byte[] destPort, Inet4Address srcIP, Inet4Address destIP, int checksum) throws IOException {
    this.srcPort = srcPort;
    this.destPort = destPort;
    this.srcIP = srcIP.getAddress();
    this.destIP = destIP.getAddress();
    ByteBuffer b = ByteBuffer.allocate(2);
    b.putInt(checksum);
    this.checksum = b.array();
    createOutgoingHeader();
  }

  public Header (byte[] header) {
    if (header.length != 16) {
      throw new IllegalArgumentException("Wrong header size.\nExpected size: 16\nActual size: " + header.length);
    }
    this.header = header;
    createIncomingHeader();
  }

  private void createOutgoingHeader() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(srcPort);
    outputStream.write(destPort);
    outputStream.write(srcIP);
    outputStream.write(destIP);
    outputStream.write(checksum);
    header = outputStream.toByteArray();
  }

  private void createIncomingHeader() {
    srcPort = Arrays.copyOfRange(header, 0, 2);
    destPort = Arrays.copyOfRange(header, 2, 4);
    srcIP = Arrays.copyOfRange(header, 4, 8);
    destIP = Arrays.copyOfRange(header, 8, 12);
    checksum = Arrays.copyOfRange(header, 12, 16);
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
