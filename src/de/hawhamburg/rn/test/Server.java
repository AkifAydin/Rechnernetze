package de.hawhamburg.rn.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

  public final int serverPort;
  public boolean serviceRequested = true;
  ServerSocket serverSocket;


  /**
   * Konstruktor
   *
   * @param serverPort
   * @throws IOException
   */
  public Server(int serverPort) throws IOException {
    this.serverPort = serverPort;
    this.serverSocket = new ServerSocket(serverPort);
  }

  @Override
  public void run() {
    while (serviceRequested) {
      try {
        Socket socket = serverSocket.accept();
        System.out.println("Neue Verbindung angenommen von " + socket.getInetAddress());
        DataInputStream socketInput = new DataInputStream(socket.getInputStream());
        DataOutputStream socketOutput = new DataOutputStream(socket.getOutputStream());
        //auf binDa Nachricht warten
        Message binDaMessage = Util.readNextMessage(socketInput);
        byte[] binDaPayload = binDaMessage.getPayload();
        String name = Util.getNameOfbinDaSender(binDaPayload);
        System.out.println(name + " hat eine Verbindung hergestellt.");
        new ChatListener(socketInput, name).start();
        // Kontakte aus der binDa Nachricht ins Telefonbuch eintragen
        Util.telefonbuchAktualisieren(binDaPayload);
        // binDa-Antwort senden
        byte[] binDa = BinDa.generate(socket); // generate binDa message
        socketOutput.write(binDa); // send binDa
        // Socket-DataOutputStream in der ChatMap speichern
        Main.newEntryInChatMap(name, socketOutput);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
