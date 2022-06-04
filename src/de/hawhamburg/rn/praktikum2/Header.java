package de.hawhamburg.rn.praktikum2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Header {

  public byte[] srcPort, destPort, srcIP, destIP, checksum;
  public byte[] header;

  public Header(byte[] srcPort, byte[] destPort, byte[] srcIP, byte[] destIP, byte[] checksum) throws IOException {
    this.srcPort = srcPort;
    this.destPort = destPort;
    this.srcIP = srcIP;
    this.destIP = destIP;
    this.checksum = checksum;
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
}
