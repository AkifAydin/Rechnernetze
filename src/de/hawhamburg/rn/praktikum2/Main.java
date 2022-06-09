package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

  public static final int PORT = 42069;
  public static RoutingTable routingTable;
  public static Inet4Address myIP;

  public static void main(String[] args) throws IOException {
    myIP = (Inet4Address) InetAddress.getByAddress(InetAddress.getLocalHost().getAddress());
    routingTable = new RoutingTable(myIP);

    new Server(PORT).start();

    System.out.println("Options:");
    System.out.println("\t• connect to <IP address>: creates a direct connection to a given peer");
    System.out.println("\t• send message to <IP address>: sends a message to a given peer");
    System.out.println("\t• close connection: closes the connection to all peers");

    Scanner scanner = new Scanner(System.in);

    while (!Thread.currentThread().isInterrupted()) {
      String command = scanner.nextLine();
      //TODO check for valid IP addresses, etc.
      if (command.startsWith("connect to ")) {
        connectTo(command);
      } else if (command.startsWith("send message to ")) {
        sendMessageTo(command);
      } else if (command.equals("close connection")) {
        closeConnection();
      }
    }

//    byte[] array = {(byte) 192, (byte) 158, 1, 38,
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

  public static void connectTo(String command) throws IOException {
    InetAddress destinationIP = InetAddress.getByName(command.substring(11));
    DataOutputStream outputStream = new DataOutputStream(new Socket(destinationIP, PORT).getOutputStream()); // direct connection to destination peer
    Header connectionHeader = new Header(myIP, (Inet4Address) destinationIP, 0);
    Message connectionMessage = new Message(connectionHeader, 1, routingTable);
    outputStream.write(connectionMessage.getMessage());
    //TODO send request 3 times, ...
    outputStream.close();
  }

  /**
   * Sends
   * @param command
   * @throws IOException
   */
  public static void sendMessageTo(String command) throws IOException {
    InetAddress destinationIP = InetAddress.getByName(command.substring(16));
    InetAddress neighbor = routingTable.getEntryByDestIP((Inet4Address) destinationIP).neighbor;
    DataOutputStream outputStream = new DataOutputStream(new Socket(neighbor, PORT).getOutputStream());
    System.out.println("Insert message: ");
    Scanner scanner = new Scanner(System.in);
    String userData = scanner.nextLine();
    Header messageHeader = new Header(myIP, (Inet4Address) destinationIP, 0);
    Message message = new Message(messageHeader,0, userData.getBytes(StandardCharsets.UTF_8));
    outputStream.write(message.getMessage());
    outputStream.close();
  }

  /**
   * Sends a closeConnection message to all peers in routing table
   * @throws IOException uh oh
   */
  public static void closeConnection() throws IOException {
    for(RoutingTable.TableEntry entry : routingTable.getTable()) {
      Inet4Address destinationIP = entry.destIP;
      InetAddress neighbor = routingTable.getEntryByDestIP((Inet4Address) destinationIP).neighbor;
      DataOutputStream outputStream = new DataOutputStream(new Socket(neighbor, PORT).getOutputStream());
      Header header = new Header(myIP, destinationIP, 0);
      Message message = new Message(header, 3);
      outputStream.write(message.getMessage());
      outputStream.close();
    }
  }

}
