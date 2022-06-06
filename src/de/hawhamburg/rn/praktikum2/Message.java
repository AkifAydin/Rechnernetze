package de.hawhamburg.rn.praktikum2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.ByteBuffer;

public class Message {

  private final Header header;
  private final int msgType;
  private short msgLen;
  private byte[] userData;
  private byte[] tableEntries; // routing table entries for message types 1 and 4
  private byte[] message;

  // message types 2, 3, 5, 6
  public Message(Header header, int msgType) throws IOException {
    this.header = header;
    this.msgType = msgType;
    this.msgLen = 4; // 1 byte for type, 1 byte for flags, 2 byte for length
    createMessage();
  }

  // message type 0
  public Message(Header header, int msgType, byte[] userData) throws IOException {
    this.header = header;
    this.msgType = msgType;
    this.msgLen = (short) (4 + userData.length);
    this.userData = userData;
    createMessage();
  }

  // message type 1, 4
  public Message(Header header, int msgType, RoutingTable table) throws IOException {
    this.header = header;
    this.msgType = msgType;
    this.msgLen = 4;
    createTableEntries(table);
    createMessage();
  }

  private void createTableEntries(RoutingTable table) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // for each entry in routing table, add IP address and lowest hop count to message body
    for (Inet4Address address : table.getTable().keySet()) {
      outputStream.write(address.getAddress());
      int index = table.getNeighborIndex(address);
      outputStream.write(Main.neighbors.get(index).getAddress());
      this.msgLen += 5; // 4 byte per IP address, 1 byte per hop count
    }
    tableEntries = outputStream.toByteArray();
  }

  private void createMessage() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(header.getHeader());
    outputStream.write(msgType);
    outputStream.write(0); // 0 byte for flags
    // 2 bytes for message length
    ByteBuffer b = ByteBuffer.allocate(2);
    b.putShort(msgLen);
    outputStream.write(b.array());
    switch (msgType) {
      case 0 -> outputStream.write(userData);
      case 1, 4 -> outputStream.write(tableEntries);
    }
    // add padding for 32 bit alignment
    for (int i = 0; i < msgLen % 4; i++) {
      outputStream.write(0);
    }
    message = outputStream.toByteArray();
  }

  public byte[] getMessage() {
    return message;
  }
}