package de.hawhamburg.rn.praktikum2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;

//TODO 32 bit alignment
public class Message {

  private final Header header;
  private final int msgType;
  private int msgLen;
  private byte[] userData;
  private byte[] tableEntries; // routing table entries for message types 1 and 4

  // message types 2, 3, 5, 6
  public Message(Header header, int msgType) {
    this.header = header;
    this.msgType = msgType;
    this.msgLen = 4; // 1 byte for type, 1 byte for flags, 2 byte for length
    //TODO build byte array for entire message
  }

  // message type 0
  public Message(Header header, int msgType, byte[] userData) {
    this(header, msgType);
    this.msgLen += userData.length;
    this.userData = userData;
  }

  // message type 1, 4
  public Message(Header header, int msgType, RoutingTable table) throws IOException {
    this(header, msgType);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    // for each entry in routing table, add IP address and lowest hop count to message body
    for (Inet4Address address: table.getTable().keySet()) {
      outputStream.write(address.getAddress());
      int index = table.getNeighborIndex(address);
      outputStream.write(Main.neighbors.get(index).getAddress());
      this.msgLen += 5; // 4 byte per IP address, 1 byte per hop count
    }
    tableEntries = outputStream.toByteArray();
  }

}