package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

  private final ServerSocket serverSocket;

  public Server(int port) throws IOException {
    this.serverSocket = new ServerSocket(port);
  }

  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {

        // A socket is an endpoint for communication between two machines.
        Socket clientSocket = serverSocket.accept();
        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

        handleMessage(new Message(inputStream.readAllBytes()), outputStream);

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleMessage(Message message, DataOutputStream outputStream) throws IOException {
    switch (message.getMsgType()) {
      case 0 -> // message
              System.out.println("Nachricht von " + message.getHeader().getDestinationIP() + ": " + message.getUserData());
      case 1 -> { // connectionRequest
        handleConnectionRequest(outputStream);
        Header connectionResponseHeader = new Header(Main.myIP, message.getHeader().getDestinationIP(), 0);
        Message connectionResponse = new Message(connectionResponseHeader, 2);
        outputStream.write(connectionResponse.getMessage());
      }
      case 2 -> // connectionResponse
              System.out.println("Verbindung zu " +  message.getHeader().getSourceIP() + " konnte aufgebaut werden.");
      case 3 -> { // closeConnection
        Main.routingTable.removeFromTable(message.getHeader().getSourceIP()); // remove sender from routing table
        handleDistanceVectorRequest(outputStream, message.getHeader().getSourceIP());
      }
      case 4 -> { // distanceVector
        if (Main.routingTable.updateTable(message.getRoutingMap(), message.getHeader().getSourceIP())) {
          handleDistanceVectorRequest(outputStream, message.getHeader().getSourceIP()); // only continue distanceVectorRouting if routing table was updated
        }
      }
      case 5 -> { // aliveRequest
        Header pingResponseHeader = new Header(Main.myIP, message.getHeader().getDestinationIP(), 0);
        Message pingResponse = new Message(pingResponseHeader, 2);
        outputStream.write(pingResponse.getMessage()); // answer with ping response
      }
      case 6 -> { // aliveNot
        Main.routingTable.removeFromTable(message.getAliveNotAddress());
        handleAliveNot(outputStream, message.getHeader().getSourceIP(), message.getAliveNotAddress());
      }

      //case 7 ->
    }
  }

  private void handleConnectionRequest(DataOutputStream outputStream) throws IOException {
    for (RoutingTable.TableEntry entry : Main.routingTable.getTable()) {
      if (entry.hopCount == 1) { // table entry for neighbor
        Header header = new Header(Main.myIP, entry.destIP, 0);
        Message message = new Message(header, 4, Main.routingTable);
        outputStream.write(message.getMessage());
      }
    }
  }

  private void handleDistanceVectorRequest(DataOutputStream outputStream, Inet4Address neighborAddress) throws IOException {
    for (RoutingTable.TableEntry entry : Main.routingTable.getTable()) {
      if (entry.hopCount == 1 && entry.destIP != neighborAddress) { // don't send package to sender of distanceVector message (split horizon)
        Header header = new Header(Main.myIP, entry.destIP, 0);
        Message message = new Message(header, 4, Main.routingTable);
        outputStream.write(message.getMessage());
      }
    }
  }

  public void handleAliveNot(DataOutputStream outputStream, Inet4Address neighborAddress, Inet4Address aliveNotAddress) throws IOException {
    for (RoutingTable.TableEntry entry : Main.routingTable.getTable()) {
      if (entry.hopCount == 1 && entry.destIP != neighborAddress) { // don't send package to sender of aliveNot message
        Header header = new Header(Main.myIP, entry.destIP, 0);
        Message message = new Message(header, 6, aliveNotAddress);
        outputStream.write(message.getMessage());
      }
    }
  }
}
