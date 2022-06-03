package de.hawhamburg.rn.test;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Util {

  public static Message readNextMessage(DataInputStream in) throws IOException {
    byte[] headerBytes = new byte[20];
    in.read(headerBytes);
    Header header = new Header(headerBytes);
    int payloadLen = header.getPayloadLen();

    byte[] payload = new byte[payloadLen];
    in.read(payload);

    return new Message(header, payload);
  }

  public static String getNameOfbinDaSender(byte[] binDaPayload) {
    byte[] b = Arrays.copyOfRange(binDaPayload, 6, binDaPayload.length);
    String[] list = new String(b).split("\0");
    return list[0];
  }

  public static void telefonbuchAktualisieren(byte[] receivedList) throws UnknownHostException {
    int count = 0; // Anzahl der Bytes des aktuellen Eintrags
    int startIndex = 6; // Index des ersten Bytes des aktuellen Eintrags
    for (int i = 6; i < receivedList.length; i++) { // Listeneinträge auslesen
      if (receivedList[i] == 0) { // Nullbyte gefunden -> Eintrag zuende
        byte[] entryBytes = new byte[count+6]; // Bytes des Eintrags
        // Bytes der Payload in die Bytes des Eintrags kopieren
        System.arraycopy(receivedList, startIndex-6, entryBytes, 0, 4);
        System.arraycopy(receivedList, startIndex-2, entryBytes, 4, 2);
        System.arraycopy(receivedList, startIndex, entryBytes, 6, count);
        addEntryTelefonbuch(entryBytes); // Teilnehmer eintragen
        // für nächsten Eintrag Zähler und Startindex zurücksetzen
        count = 0;
        i+=6;
        startIndex = i + 1;
      } else { // Eintrag noch nicht zuende
        count++; // nächstes Zeichen überprüfen
      }
    }
  }

  private static void addEntryTelefonbuch(byte[] entry) throws UnknownHostException {
    InetSocketAddress isA;
    isA = new InetSocketAddress(getEntryInetAddress(entry), getEntryPort(entry));
    Main.newEntryInTelefonbuch(getEntryName(entry), isA);
  }

  private static String getEntryName(byte[] entry) {
    byte[] nameBytes = new byte[entry.length - 6];
    System.arraycopy(entry, 6, nameBytes, 0, nameBytes.length);
    return new String(nameBytes);
  }

  private static InetAddress getEntryInetAddress(byte[] entry) throws UnknownHostException {
    byte[] ipBytes = new byte[4];
    System.arraycopy(entry, 0, ipBytes, 0, 4);
    return Inet4Address.getByAddress(ipBytes);
  }

  private static int getEntryPort(byte[] entry) {
    return byteToPositiveInt(entry[4]) * 256 + byteToPositiveInt(entry[5]);
  }

  public static int byteToPositiveInt(byte b) {
    return ((int) b & 0xFF);
  }

  public static byte[] intToLowerTwoBytes(int port) {
    byte[] bytes = new byte[2];
    bytes[0] = (byte) (port >> 8);
    bytes[1] = (byte) port;
    return bytes;
  }

  // chop up an int into its 4 bytes
  public static byte[] intToBytes(int i) {
    return new byte[]{
            (byte) (i >> 24),
            (byte) (i >> 16),
            (byte) (i >> 8),
            (byte) i
    };
  }

  // glue together 4 bytes into an int
  public static int bytesToInt(byte[] bytes) {
    return ((((int) bytes[0]) & 0xFF) << 24)
            | ((((int) bytes[1]) & 0xFF) << 16)
            | ((((int) bytes[2]) & 0xFF) << 8)
            | (((int) bytes[3]) & 0xFF);
  }
}
