package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
        clientSocket.close();
      }
      serverSocket.close();
    } catch (IOException e) {
      // do nothing
    }
  }

  private void handleMessage(Message message, DataOutputStream outputStream) throws IOException {
    switch (message.getMsgType()) {
      case 0 -> // message
              System.out.println("Nachricht von " + message.getHeader().getDestinationIP() + ": " + message.getUserData());
      case 1 -> { // connectionRequest
        // send connectionResponse
        Header connectionResponseHeader = new Header(Main.myIP, message.getHeader().getDestinationIP(), 0);
        Message connectionResponse = new Message(connectionResponseHeader, 2);
        outputStream.write(connectionResponse.getMessage());

        Main.startDistanceVector();
        new AliveFunction((message.getHeader().getDestinationIP())).start();
      }
      //case 2 -> connectionResponse
      case 3 -> { // closeConnection
        Main.routingTable.removeFromTable(message.getHeader().getSourceIP()); // remove sender from routing table
        Main.startDistanceVector();
        //TODO interrupt AliveFunction for sender if sender is a neighbor
      }
      case 4 -> { // distanceVector
        if (Main.routingTable.updateTable(message.getRoutingMap(), message.getHeader().getSourceIP())) {
          Main.startDistanceVectorIgnore(message.getHeader().getSourceIP()); // only continue distanceVectorRouting if routing table was updated
        }
      }
      //case 5 -> { // aliveRequest
      case 6 -> { // aliveNot
        Main.routingTable.removeFromTable(message.getAliveNotAddress());
        Main.startDistanceVector();
        //TODO interrupt AliveFunction aliveNotAddress if aliveNotAddress is a neighbor
      }
      //case 7 -> aliveResponse
    }
  }
}
