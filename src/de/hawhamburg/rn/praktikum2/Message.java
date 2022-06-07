package de.hawhamburg.rn.praktikum2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Message {

  private final Header header;
  private final int msgType;
  private short msgLen;
  private byte[] userData;
  private final Map<Inet4Address, Byte> routingMap = new HashMap<>(); // for incoming messages
  private byte[] routingMapArray; // for outgoing messages
  private Inet4Address aliveNotAddress;
  private byte[] message;

  // message types 2, 3, 5
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
    createTableEntriesArray(table);
    createMessage();
  }

  // message type 6
  public Message(Header header, int msgType, Inet4Address aliveNotAddress) throws IOException {
    this.header = header;
    this.msgType = msgType;
    this.msgLen = 8; // 1 byte for type, 1 byte for flags, 2 byte for length, 4 byte for address
    this.aliveNotAddress = aliveNotAddress;
    createMessage();
  }

  public Message(byte[] message) throws UnknownHostException {
    header = new Header(Arrays.copyOfRange(message, 0, 16));
    // message type
    msgType = message[16];
    // message length
    ByteBuffer bb = ByteBuffer.allocate(2);
    bb.put(message[18]);
    bb.put(message[19]);
    msgLen = bb.getShort(0);

    switch (msgType) {
      case 0 -> userData = Arrays.copyOfRange(message, 20, message.length);
      case 1, 4 -> createTableEntriesMap(Arrays.copyOfRange(message, 20, message.length));
      case 6 -> aliveNotAddress = (Inet4Address) InetAddress.getByAddress(Arrays.copyOfRange(message, 20, 24));
    }
  }

  private void createTableEntriesMap(byte[] entries) throws UnknownHostException {
    for (int i = 0; i < entries.length; i += 5) {
      if (i + 5 < entries.length) { // skip over padding if less than 5 more bytes available
        // add IP address and hop count to map
        routingMap.put((Inet4Address) Inet4Address.getByAddress(Arrays.copyOfRange(entries, i, i + 4)), entries[i + 4]);
      }
    }
  }

  private void createTableEntriesArray(RoutingTable table) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // for each entry in routing table, add IP address and lowest hop count to message body
    for (RoutingTable.TableEntry entry : table.getTable()) {
      outputStream.write(entry.destIP.getAddress());
      outputStream.write(entry.hopCount);
      this.msgLen += 5; // 4 byte per IP address, 1 byte per hop count
    }
    routingMapArray = outputStream.toByteArray();
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
      case 1, 4 -> outputStream.write(routingMapArray);
      case 6 -> outputStream.write(aliveNotAddress.getAddress());
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

  public Header getHeader() {
    return header;
  }

  public int getMsgType() {
    return msgType;
  }

  public String getUserData() {
    return new String(userData);
  }

  public Map<Inet4Address, Byte> getRoutingMap() {
    return routingMap;
  }

  public Inet4Address getAliveNotAddress() {
    return aliveNotAddress;
  }
}