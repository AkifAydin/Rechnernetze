package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Starts all the necessary threads for this application. Also contains the client functions.
 */
public class Main {

  public static int PORT = 42069; // standard port
  public static RoutingTable routingTable; // routing table
  public static Inet4Address myIP; // local IP address
  public static final int DVR_WAIT = 5000; // time between automatic distance vector requests (in ms)
  public static final int ALIVE_WAIT = 2000; // time between alive requests (in ms)
  public static final int CR_WAIT = 1000; // time between connection requests (in ms)

  public static void main(String[] args) throws IOException, InterruptedException {

    myIP = (Inet4Address) InetAddress.getByName("25.80.92.51");//(Inet4Address) InetAddress.getByAddress(InetAddress.getLocalHost().getAddress());
    routingTable = new RoutingTable(myIP);
    System.out.println(routingTable.getEntries().get(0).destIP);

    // start additional threads
    Server server = new Server(PORT);
    server.start();

    AliveFunction alive = new AliveFunction();
    alive.start();

    DistanceVectorRouting dvr = new DistanceVectorRouting();
    dvr.start();

    // client
    System.out.println("Options:");
    System.out.println("\t• connect to <IP address>: creates a direct connection to a given peer");
    System.out.println("\t• send message to <IP address>: sends a message to a given peer");
    System.out.println("\t• close connection: closes the connection to all peers");

    // read user input
    Scanner scanner = new Scanner(System.in);

    while (true) {
      String command = scanner.nextLine();
      if (command.startsWith("connect to ")) {
        try {
          connectTo(InetAddress.getByName(command.substring(11)));
        } catch (UnknownHostException e) {
          System.err.println("Invalid IP address.");
        }
      } else if (command.startsWith("send message to ")) {
        try {
          sendMessageTo(InetAddress.getByName(command.substring(16)));
        } catch (UnknownHostException e) {
          System.err.println("Invalid IP address.");
        }
      } else if (command.equals("close connection")) {
        closeConnection();
        // properly stop all running threads
        interruptServer(server);
        alive.interrupt();
        dvr.interrupt();
        break;
      } else if (command.equals("show table")) {
        for (int i = 0; i < routingTable.getEntries().size(); i++) {
          RoutingTable.TableEntry entry = routingTable.getEntries().get(i);
          System.out.println(entry.destIP + "\t" + entry.hopCount + "\t" + entry.neighbor);
          System.out.println();
        }
      } else {
        System.err.println("Invalid command.");
      }
    }
  }

  /**
   * Connects to a specified target peer. Sends up to 3 connectionRequests if target peer doesn't respond.
   *
   * @param destinationIP IP address of the target peer
   */
  public static void connectTo(InetAddress destinationIP) throws IOException {
    try {
      Socket clientSocket = new Socket(destinationIP, PORT); // direct connection to destination peer
      clientSocket.setSoTimeout(CR_WAIT); // read call on input stream will only wait a certain amount of time
      DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
      DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
      int counter = 0;
      while (counter < 3) {
        // send connectionRequest
        Header connectionHeader = new Header(myIP, (Inet4Address) destinationIP, 0);
        Message connectionMessage = new Message(connectionHeader, 1, routingTable);
        outputStream.write(connectionMessage.getMessage());
        outputStream.flush();
        // wait for and handle connectionResponse
        try {
          Message messageIn = new Message(inputStream.readNBytes(16)); // throws exception after 1 second of not being able to read the specified bytes
          if (messageIn.getMsgType() == 2) { // check whether message type == 2 (connectionResponse)
            System.out.println("Connection to " + destinationIP.getHostAddress() + " was successfully established.");
            // add new entry to routing table
            routingTable.addEntry((Inet4Address) destinationIP);
            break;
          } else if (++counter == 3) { // print out failure message after 3rd unsuccessful iteration of the loop
            System.err.println("Connection to " + destinationIP.getHostAddress() + " could not be established.");
          }
        } catch (SocketTimeoutException e) {
          if (++counter == 3) { // print out failure message after 3rd unsuccessful iteration of the loop
            System.err.println("Connection to " + destinationIP.getHostAddress() + " could not be established.");
          }
        }
      }
      inputStream.close();
      outputStream.close();
    } catch (ConnectException e) {
      System.err.println("Connection to " + destinationIP.getHostAddress() + " could not be established (timed out).");
    }
  }

  /**
   * Sends a message to a specified target peer.
   *
   * @param destinationIP IP address of the target peer
   */
  public static void sendMessageTo(InetAddress destinationIP) throws IOException {
    try {
      RoutingTable.TableEntry entry = routingTable.getEntryByDestIP((Inet4Address) destinationIP);
      if (entry == null) {
        System.err.println("Can't find requested peer in the routing table. Message wasn't sent.");
      } else {
        DataOutputStream outputStream = new DataOutputStream(new Socket(entry.neighbor, PORT).getOutputStream());
        System.out.println("Insert message: ");
        Scanner scanner = new Scanner(System.in);
        String userData = scanner.nextLine();
        Header messageHeader = new Header(myIP, (Inet4Address) destinationIP, 0);
        Message message = new Message(messageHeader, 0, userData.getBytes(StandardCharsets.UTF_8));
        outputStream.write(message.getMessage());
        outputStream.flush();
        outputStream.close();
      }
    } catch (ConnectException e) {
      System.err.println("Connection timed out. Message could not be sent.");
    }
  }

  /**
   * Sends a closeConnection message to all peers in routing table except yourself.
   */
  public static void closeConnection() throws IOException {
    for (RoutingTable.TableEntry entry : routingTable.getEntries()) {
      if (entry.hopCount != 0) {
        Inet4Address destinationIP = entry.destIP;
        InetAddress neighbor = routingTable.getEntryByDestIP(destinationIP).neighbor;
        DataOutputStream outputStream = new DataOutputStream(new Socket(neighbor, PORT).getOutputStream());
        Header header = new Header(myIP, destinationIP, 0);
        Message message = new Message(header, 3);
        outputStream.write(message.getMessage());
        outputStream.flush();
        outputStream.close();
      }
    }
  }

  /**
   * Interrupts the server thread.
   * @param server the server thread
   */
  public static void interruptServer(Server server) throws IOException {
    server.interrupt();

    DataOutputStream outputStream = new DataOutputStream(new Socket(myIP, PORT).getOutputStream());
    Header header = new Header(myIP, myIP, 0);
    Message message = new Message(header, 8); // stopServer message (necessary if server thread is busy waiting for accept())
    outputStream.write(message.getMessage());
    outputStream.flush();
    outputStream.close();
  }
}