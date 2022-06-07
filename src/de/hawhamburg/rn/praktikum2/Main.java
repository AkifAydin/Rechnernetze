package de.hawhamburg.rn.praktikum2;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

public class Main {

  public static final byte[] PORT = {(byte) 164, 85}; // 42069 in 2 bytes
  public static RoutingTable routingTable;
  public static Inet4Address myIP;

  public static void main(String[] args) throws IOException {
    myIP = (Inet4Address) InetAddress.getByAddress(InetAddress.getLocalHost().getAddress());
    routingTable = new RoutingTable(myIP);

    // new Server(PORT).start();

//    byte[] array = {(byte) 164, 85, (byte) 164, 85,
//            (byte) 192, (byte) 158, 1, 38,
//            (byte) 192, (byte) 158, 1, 39,
//            0, 0, 0, 0,
//            1, 0, 0, 16,
//            (byte) 192, (byte) 158, 1, 38,
//            1,
//            (byte) 192, (byte) 158, 1, 39,
//            1,
//            0, 0};
//    Message message = new Message(array);
//    Header header = message.getHeader();
//    System.out.println(header.getSourceIP());
//    System.out.println(header.getDestinationIP());
  }

}
