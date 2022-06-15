package de.hawhamburg.rn.praktikum2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Periodically starts the distance vector routing algorithm and sends out distanceVector messages to all neighbors.
 */
public class DistanceVectorRouting extends Thread {

  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        startDistanceVector();
        sleep(Main.DVR_WAIT);
      }
    } catch (IOException | InterruptedException e) {
      // do nothing
    }
  }

  /**
   * Sends a distanceVector message to all neighbors.
   */
  public static void startDistanceVector() throws IOException {
    for (RoutingTable.TableEntry entry : Main.routingTable.getEntries()) {
      if (entry.hopCount == 1) { // table entry for neighbor
        sendDistanceVector(entry.destIP);
      }
    }
  }

  /**
   * Sends a distanceVector message to all neighbors except one.
   *
   * @param ignoreAddress ignored neighbor
   */
  public static void continueDistanceVector(Inet4Address ignoreAddress) throws IOException {
    for (RoutingTable.TableEntry entry : Main.routingTable.getEntries()) {
      if (entry.hopCount == 1 && !entry.destIP.equals(ignoreAddress)) { // don't send package to sender of distanceVector message (split horizon)
        sendDistanceVector(entry.destIP);
      }
    }
  }

  /**
   * Sends a distanceVector message to the specified peer.
   *
   * @param destinationIP target peer
   */
  private static void sendDistanceVector(Inet4Address destinationIP) throws IOException {
    Inet4Address neighbor = Main.routingTable.getEntryByDestIP(destinationIP).neighbor;
    Socket socket = new Socket(neighbor, Main.PORT);
    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
    Header header = new Header(Main.myIP, destinationIP, 0);
    Message message = new Message(header, 4, Main.routingTable);
    outputStream.write(message.getMessage());
    outputStream.close();
  }
}