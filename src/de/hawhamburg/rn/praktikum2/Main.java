package de.hawhamburg.rn.praktikum2;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

  public static final int PORT = 42069;
  public static RoutingTable routingTable;
  public static Inet4Address myIP;

  public static void main(String[] args) throws IOException, InterruptedException {
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
        connectTo(InetAddress.getByName(command.substring(11)));
      } else if (command.startsWith("send message to ")) {
        sendMessageTo(InetAddress.getByName(command.substring(16)));
      } else if (command.equals("close connection")) {
        closeConnection();
      }
    }
  }

  /**
   * Connects to a specified target peer. Sends up to 3 connectionRequests if target peer doesn't respond.
   *
   * @param destinationIP IP address of the target peer
   * @throws IOException uh oh
   */
  public static void connectTo(InetAddress destinationIP) throws IOException {
    Socket clientSocket = new Socket(destinationIP, PORT);
    clientSocket.setSoTimeout(1000); // read call on input stream will only wait for 1 second
    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream()); // direct connection to destination peer
    DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
    int counter = 0;
    while (counter < 3) {
      // send connectionRequest
      Header connectionHeader = new Header(myIP, (Inet4Address) destinationIP, 0);
      Message connectionMessage = new Message(connectionHeader, 1, routingTable);
      outputStream.write(connectionMessage.getMessage());
      // wait for and handle connectionResponse
      try {
        byte[] lenAry = new byte[2];
        inputStream.readNBytes(lenAry, 14, 2); // throws exception after 1 second of not being able to read the specified bytes
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(lenAry[0]);
        buffer.put(lenAry[1]);
        if (buffer.getShort(0) == 2) { // check whether message type == 2 (connectionResponse)
          System.out.println("Verbindung zu " + destinationIP + " wurde erfolgreich aufgebaut.");
          break;
        } else if (++counter == 3) { // print out failure message after 3rd unsuccessful iteration of the loop
          System.out.println("Verbindung zu " + destinationIP + " konnte nicht aufgebaut werden.");
        }
      } catch (SocketTimeoutException e) {
        if (++counter == 3) { // print out failure message after 3rd unsuccessful iteration of the loop
          System.out.println("Verbindung zu " + destinationIP + " konnte nicht aufgebaut werden.");
        }
      }
    }
    inputStream.close();
    outputStream.close();
  }

  /**
   * Sends a message to a specified target peer.
   *
   * @param destinationIP IP address of the target peer
   * @throws IOException bad
   */
  public static void sendMessageTo(InetAddress destinationIP) throws IOException {
    InetAddress neighbor = routingTable.getEntryByDestIP((Inet4Address) destinationIP).neighbor;
    DataOutputStream outputStream = new DataOutputStream(new Socket(neighbor, PORT).getOutputStream());
    System.out.println("Insert message: ");
    Scanner scanner = new Scanner(System.in);
    String userData = scanner.nextLine();
    Header messageHeader = new Header(myIP, (Inet4Address) destinationIP, 0);
    Message message = new Message(messageHeader, 0, userData.getBytes(StandardCharsets.UTF_8));
    outputStream.write(message.getMessage());
    outputStream.close();
  }

  /**
   * Sends a closeConnection message to all peers in routing table.
   *
   * @throws IOException not good
   */
  public static void closeConnection() throws IOException {
    for (RoutingTable.TableEntry entry : routingTable.getTable()) {
      Inet4Address destinationIP = entry.destIP;
      InetAddress neighbor = routingTable.getEntryByDestIP((Inet4Address) destinationIP).neighbor;
      DataOutputStream outputStream = new DataOutputStream(new Socket(neighbor, PORT).getOutputStream());
      Header header = new Header(myIP, destinationIP, 0);
      Message message = new Message(header, 3);
      outputStream.write(message.getMessage());
      outputStream.close();
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
