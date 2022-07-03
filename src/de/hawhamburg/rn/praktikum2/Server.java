package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

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

        // read first 16 bytes to extract message length
        byte[] firstSixteen = inputStream.readNBytes(16);
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.put(firstSixteen[14]);
        bb.put(firstSixteen[15]);
        int msgLen = bb.getShort(0);
        byte[] msgArray = Arrays.copyOf(firstSixteen, 12 + msgLen);
        inputStream.readNBytes(msgArray, 16, msgLen - 4);

        Message message = new Message(msgArray);
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
              outputStream.flush();
              outputStream.close();

              Main.routingTable.updateTable(message.getRoutingMap(), message.getHeader().getSourceIP());

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
            case 5 -> { // == aliveRequest -> handled by the AliveFunction thread
              Header aliveResponseHeader = new Header(Main.myIP, message.getHeader().getSourceIP(), 0);
              Message aliveResponse = new Message(aliveResponseHeader, 7);
              DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
              outputStream.write(aliveResponse.getMessage());
              outputStream.flush();
              outputStream.close();
            }
            case 6 -> { // == aliveNot
              Main.routingTable.removeFromTable(message.getAliveNotAddress());
              DistanceVectorRouting.startDistanceVector();
            }
            //case 7 == aliveResponse -> handled by the AliveFunction thread
            case 8 -> { //== stopServer
              // do nothing
            }
          }
        } else { // forward message
          InetAddress nextHop = Main.routingTable.getEntryByDestIP(message.getHeader().getDestinationIP()).neighbor;
          Socket forwardSocket = new Socket(nextHop, Main.PORT);
          DataOutputStream outputStream = new DataOutputStream(forwardSocket.getOutputStream());
          outputStream.write(message.getMessage());
          outputStream.flush();
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