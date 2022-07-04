package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ConcurrentModificationException;

/**
 * Periodically checks whether the direct neighbors are still available.
 */
public class AliveFunction extends Thread {

  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        for (RoutingTable.TableEntry entry : Main.routingTable.getEntries()) {
          if (entry.hopCount == 1) { // table entry for neighbor
            try {
              Socket socket = new Socket(entry.destIP, Main.PORT);
              socket.setSoTimeout(Main.ALIVE_WAIT); // make read requests only wait for a certain amount of time
              DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
              DataInputStream inputStream = new DataInputStream(socket.getInputStream());

              // send aliveRequest
              Header header = new Header(Main.myIP, entry.destIP, 0);
              Message message = new Message(header, 5);
              outputStream.write(message.getMessage());
              outputStream.flush();

              try {
                // wait for aliveResponse
                Message messageIn = new Message(inputStream.readNBytes(16));
                if (messageIn.getMsgType() != 7) { // check whether message type == 7 (aliveResponse)
                  connectionLost(entry.destIP);
                }
              } catch (SocketTimeoutException e) {
                connectionLost(entry.destIP);
              }
              outputStream.close();
              inputStream.close();
            } catch (IOException e) {
              // do nothing
            }
          }
        }
        sleep(Main.ALIVE_WAIT);
      }
    } catch (InterruptedException | ConcurrentModificationException e) {
      // do nothing
    }
  }

  /**
   * Sends aliveNot messages to all peers to notify them about a lost connection to the current neighbor.
   */
  private void connectionLost(Inet4Address neighbor) throws IOException {
    System.err.println("Connection to " + neighbor + " lost. Notifying other peers.");
    Main.routingTable.removeFromTable(neighbor);
    for (RoutingTable.TableEntry entry : Main.routingTable.getEntries()) {
      Socket socket = new Socket(entry.neighbor, Main.PORT);
      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
      Header aliveNotHeader = new Header(Main.myIP, neighbor, 0);
      Message aliveNotMessage = new Message(aliveNotHeader, 6, neighbor);
      outputStream.write(aliveNotMessage.getMessage());
      outputStream.flush();
      outputStream.close();
    }
  }
}