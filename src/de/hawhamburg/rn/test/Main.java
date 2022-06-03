package de.hawhamburg.rn.test;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
  private static Scanner inFromUser = new Scanner(System.in);
  private static Map<String, DataOutputStream> chatMap = new HashMap<>();
  protected static Map<String, InetSocketAddress> telefonbuch = new HashMap<>();
  public static final int MY_PORT = 60000;
  public static String myName;

  public static void main(String[] args) throws IOException {
    // erster Eintrag im Telefonbuch
    telefonbuch.put("Denise", new InetSocketAddress(InetAddress.getByName("192.168.178.32"), 60000));
    telefonbuch.put("Fin1", new InetSocketAddress(InetAddress.getByName("192.168.178.34"), 60000));
    telefonbuch.put("Fin2", new InetSocketAddress(InetAddress.getByName("192.168.178.52"), 60000));

    new Server(MY_PORT).start();
    System.out.println("Bitte Namen eingeben");
    myName = inFromUser.nextLine();
    System.out.println("Hallo " + myName);

    while (true) {
      String request = inFromUser.nextLine();
      //Verbindung aufbauen
      String newConnection = "Chat mit ";
      if (request.startsWith(newConnection)) {
        String name = request.substring(newConnection.length());
        startConnection(name);
      }
      // Sende Nachricht an Jan: Hi wie gehts?
      String nextMessage = "an ";
      int firstColonIndex = request.indexOf(':');
      if (request.startsWith(nextMessage)) {
        String name = request.substring(nextMessage.length(), firstColonIndex);
        String message = request.substring(firstColonIndex+2);
        sendMessage(name, message);
      }
      //if (request.equals("Quit")) {}
    }
  }

  public static void startConnection(String name) throws IOException {
    InetSocketAddress adr = telefonbuch.get(name);
    Socket socket = new Socket(adr.getAddress(), adr.getPort());
    System.out.println("Verbindung zu " + name + " hergestellt.");
    DataInputStream socketInput = new DataInputStream(socket.getInputStream());
    DataOutputStream socketOutput = new DataOutputStream(socket.getOutputStream());
    byte[] binDa = BinDa.generate(socket); // generate binDa message
    socketOutput.write(binDa); // send binDa
    Message binDaMessage = Util.readNextMessage(socketInput); // wait for binDa answer
    Util.telefonbuchAktualisieren(binDaMessage.getPayload()); // update contacts
    newEntryInChatMap(name, socketOutput);
    new ChatListener(socketInput, name).start();
  }

  public static void sendMessage(String name, String message) throws IOException {
    InetSocketAddress adr = telefonbuch.get(name);
    DataOutputStream out = chatMap.get(name);
    if (out == null) {
      System.out.println("Kein aktiver Chat mit " + name);
    } else {
      System.out.println("Sende \"" + message + "\" an " + name);
      byte[] msg = genMessage(adr.getAddress(), adr.getPort(), message);
      out.write(msg);
    }
  }

  public static synchronized void newEntryInTelefonbuch(String name, InetSocketAddress inetSocketAddress) {
    telefonbuch.put(name, inetSocketAddress);
  }

  public static synchronized void newEntryInChatMap(String name, DataOutputStream out) {
    chatMap.put(name, out);
  }

  public static byte[] genMessage(InetAddress addressReceiver, int portReceiver, String payload) throws IOException {
    byte[] pl = payload.getBytes(StandardCharsets.UTF_8);
    Header header = new Header(addressReceiver, portReceiver, MY_PORT, 3, pl);
    Message msg = new Message(header, pl);
    return msg.toBytes();
  }

  //public static void verbindungBeenden() {}
}
