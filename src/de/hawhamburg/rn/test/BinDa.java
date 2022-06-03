package de.hawhamburg.rn.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BinDa {

  public static byte[] generate(Socket receiver) throws IOException {
    byte[] binDaPayload = binDaPayload();
    Header header = new Header(receiver, Main.MY_PORT, 1, binDaPayload);
    Message binDa = new Message(header, binDaPayload);
    return binDa.toBytes();
  }

  private static byte[] binDaPayload() throws IOException {
    ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
    payloadStream.write(binDaEntrySelf());
    payloadStream.write(binDaList());
    return payloadStream.toByteArray();
  }

  /**
   * Generates the first entry in binDa list
   * containing own connection data
   * @return first list entry as byte array
   * @throws IOException
   */
  private static byte[] binDaEntrySelf() throws IOException {
    byte[] ownIP = Inet4Address.getLocalHost().getAddress();
    byte[] ownPort = Util.intToLowerTwoBytes(Main.MY_PORT);
    byte[] ownName = Main.myName.getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.write(ownIP);
    stream.write(ownPort);
    stream.write(ownName);
    stream.write(0);
    return stream.toByteArray();
  }

  /**
   * Adds entries from telefonbuch to list in binDa
   * @return the binDa list
   * @throws IOException
   */
  private static byte[] binDaList() throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    for (Map.Entry<String, InetSocketAddress> contact : Main.telefonbuch.entrySet()) {
      InetSocketAddress ipPort = contact.getValue();
      stream.write(ipPort.getAddress().getAddress());                        // add IP
      stream.write(Util.intToLowerTwoBytes(ipPort.getPort())); // add port
      stream.write(contact.getKey().getBytes(StandardCharsets.UTF_8));       // add name
      stream.write(0);
    }
    return stream.toByteArray();
  }
}
