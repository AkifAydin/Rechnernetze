package de.hawhamburg.rn.praktikum2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class AliveFunction extends Thread {

  private InetAddress neighbor;

  public AliveFunction(InetAddress neighbor) {
    this.neighbor = neighbor;
  }

  public void run() {
    try {
      Socket socket = new Socket(neighbor, Main.PORT);
      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
      DataInputStream inputStream = new DataInputStream(socket.getInputStream());

      while (!Thread.currentThread().isInterrupted()) {

        Header header = new Header(Main.myIP, (Inet4Address) neighbor, 0);
        Message message = new Message(header, 5);
        outputStream.write(message.getMessage());

        Thread.currentThread().wait(667); // wait for 2/3 seconds

        try {
          byte[] lenAry = new byte[1];
          inputStream.readNBytes(lenAry, 12, 1);
          ByteBuffer buffer = ByteBuffer.allocate(1);
          buffer.put(lenAry[0]);
          if (buffer.get(0) != 7) { // check whether message type == 7 (aliveResponse)
            throw new IOException();
          }
        } catch (IOException e) {
          connectionLost();
        }
      }
      outputStream.close();
      inputStream.close();
    } catch (IOException | InterruptedException e) {
      // do nothing
    }
  }

  private void connectionLost() throws IOException {
    System.out.println("Connection to " +  neighbor + " lost.");
    for (RoutingTable.TableEntry entry : Main.routingTable.getEntries()) {
      Socket socket = new Socket(entry.destIP, Main.PORT);
      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
      Header aliveNotHeader = new Header(Main.myIP, (Inet4Address) neighbor, 0);
      Message aliveNotMessage = new Message(aliveNotHeader, 6, (Inet4Address) neighbor);
      outputStream.write(aliveNotMessage.getMessage());
    }
  }

}
