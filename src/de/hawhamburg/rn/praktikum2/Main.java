package de.hawhamburg.rn.praktikum2;

import java.io.IOException;

public class Main {

  protected static final int PORT = 42069;
  protected static RoutingTable routingTable;

  public static void main(String[] args) throws IOException {
    new Server(PORT).start();

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
