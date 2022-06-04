package de.hawhamburg.rn.praktikum2;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

public class Main {

  protected static final int PORT = 42069;
  protected static List<Inet4Address> neighbors = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    new Server(PORT).start();
  }

}
