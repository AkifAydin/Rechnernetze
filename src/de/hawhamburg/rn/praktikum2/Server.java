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
      // A socket is an endpoint for communication between two machines.
      Socket clientSocket = serverSocket.accept();
      DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
      DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
      // TODO handle incoming messages
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
