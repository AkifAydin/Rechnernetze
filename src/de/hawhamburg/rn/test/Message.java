package de.hawhamburg.rn.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Message {

  private Header header;
  private byte[] payload;

  public Message(Header header, byte[] payload) {
    this.header = header;
    this.payload = payload;
  }

  public Header getHeader() {
    return header;
  }

  public void setHeader(Header header) {
    this.header = header;
  }

  public byte[] getPayload() {
    return payload;
  }

  public void setPayload(byte[] payload) {
    this.payload = payload;
  }

  public byte[] toBytes() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    header.compose();
    outputStream.write(header.getFullHeader());
    outputStream.write(payload);
    return outputStream.toByteArray();
  }
}
