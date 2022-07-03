//package de.hawhamburg.rn.praktikum2;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.net.SocketTimeoutException;
//
///**
// * Checks whether a given neighbor is still available.
// */
//public class IsNeighborAlive extends Thread {
//
//  private final InetAddress neighbor;
//
//  public IsNeighborAlive(InetAddress neighbor) {
//    this.neighbor = neighbor;
//  }
//
//  public void run() {
//    try {
//      Socket socket = new Socket(neighbor, Main.PORT);
//      socket.setSoTimeout(Main.ALIVE_WAIT); // make read requests only wait for a certain amount of time
//      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
//      DataInputStream inputStream = new DataInputStream(socket.getInputStream());
//
//      // send aliveRequest
//      Header header = new Header(Main.myIP, (Inet4Address) neighbor, 0);
//      Message message = new Message(header, 5);
//      outputStream.write(message.getMessage());
//      outputStream.flush();
//
//      try {
//        // wait for aliveResponse
//        Message messageIn = new Message(inputStream.readNBytes(16));
//        if (messageIn.getMsgType() != 7) { // check whether message type == 7 (aliveResponse)
//          connectionLost();
//        }
//      } catch (SocketTimeoutException e) {
//        connectionLost();
//      }
//      outputStream.close();
//      inputStream.close();
//    } catch (IOException e) {
//      // do nothing
//    }
//  }
//
//  /**
//   * Sends aliveNot messages to all peers to notify them about a lost connection to the current neighbor.
//   */
//  private void connectionLost() throws IOException {
//    System.out.println("Connection to " + neighbor + " lost.");
//    Main.routingTable.removeFromTable((Inet4Address) neighbor);
//    for (RoutingTable.TableEntry entry : Main.routingTable.getEntries()) {
//      Socket socket = new Socket(entry.neighbor, Main.PORT);
//      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
//      Header aliveNotHeader = new Header(Main.myIP, (Inet4Address) neighbor, 0);
//      Message aliveNotMessage = new Message(aliveNotHeader, 6, (Inet4Address) neighbor);
//      outputStream.write(aliveNotMessage.getMessage());
//      outputStream.flush();
//      outputStream.close();
//    }
//  }
//}