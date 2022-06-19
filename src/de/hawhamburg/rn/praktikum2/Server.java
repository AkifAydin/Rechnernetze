package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Handles incoming messages.
 */
public class Server extends Thread {

  private final ServerSocket serverSocket; // Listens for any connections to be made on the port and accepts them.

  public Server(int port) throws IOException {
    this.serverSocket = new ServerSocket(port);
  }

  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {

        Socket socket = serverSocket.accept(); // direct connection to destination peer
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());

        Message message = new Message(inputStream.readAllBytes());
        if (message.getHeader().getDestinationIP().equals(Main.myIP)) { // message sent directly to this peer?
          switch (message.getMsgType()) {
            case 0 -> // == message
                    System.out.println("Message from " + message.getHeader().getSourceIP().getHostAddress() + ": " + message.getUserData());
            case 1 -> { // == connectionRequest
              // send connectionResponse
              Header connectionResponseHeader = new Header(Main.myIP, message.getHeader().getSourceIP(), 0);
              Message connectionResponse = new Message(connectionResponseHeader, 2);
              DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
              outputStream.write(connectionResponse.getMessage());
              outputStream.close();

              DistanceVectorRouting.startDistanceVector();
            }
            //case 2 == connectionResponse -> handled by the Main/Client thread
            case 3 -> { // == closeConnection
              Main.routingTable.removeFromTable(message.getHeader().getSourceIP()); // remove sender from routing table
              DistanceVectorRouting.startDistanceVector();
            }
            case 4 -> { // == distanceVector
              if (Main.routingTable.updateTable(message.getRoutingMap(), message.getHeader().getSourceIP())) {
                DistanceVectorRouting.continueDistanceVector(message.getHeader().getSourceIP()); // only continue if routing table was updated
              }
            }
            //case 5 == aliveRequest -> handled by the AliveFunction thread
            case 6 -> { // == aliveNot
              Main.routingTable.removeFromTable(message.getAliveNotAddress());
              DistanceVectorRouting.startDistanceVector();
            }
            //case 7 == aliveResponse -> handled by the AliveFunction thread
          }
        } else { // forward message
          InetAddress nextHop = Main.routingTable.getEntryByDestIP(message.getHeader().getDestinationIP()).neighbor;
          Socket forwardSocket = new Socket(nextHop, Main.PORT);
          DataOutputStream outputStream = new DataOutputStream(forwardSocket.getOutputStream());
          outputStream.write(message.getMessage());
          outputStream.close();
        }
        socket.close();
      }
      serverSocket.close();
    } catch (IOException e) {
      // do nothing
    }
  }
}